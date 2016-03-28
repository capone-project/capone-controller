/*
 * Copyright (C) 2016 Patrick Steinhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package im.pks.sd.controller.query;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;
import im.pks.sd.services.GenericActivity;
import im.pks.sd.services.InvokeActivity;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.QueryTask;
import im.pks.sd.services.Services;
import org.abstractj.kalium.keys.SigningKey;
import org.apache.commons.lang.StringUtils;

public class ServiceDetailActivity extends Activity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE = "service";
    public static final String EXTRA_SERVICE_DETAILS = "service_details";

    private ProgressDialog progressDialog;
    private ArrayAdapter<ServiceDetails.Parameter> parameterAdapter;
    private ServiceDetails serviceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        Intent intent = getIntent();
        Server server = (Server) intent.getSerializableExtra(EXTRA_SERVER);
        Service service = (Service) intent.getSerializableExtra(EXTRA_SERVICE);

        ImageView serviceImage = (ImageView) findViewById(R.id.service_image);
        serviceImage.setImageResource(Services.getImageId(service.type));
        TextView serverKey = (TextView) findViewById(R.id.server_key);
        serverKey.setText(server.publicKey);
        TextView serverAddress = (TextView) findViewById(R.id.server_address);
        serverAddress.setText(server.address);
        TextView serviceName = (TextView) findViewById(R.id.service_name);
        serviceName.setText(service.name);
        TextView servicePort = (TextView) findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(service.port));
        TextView serviceType = (TextView) findViewById(R.id.service_type);
        serviceType.setText(service.type);

        parameterAdapter = new ArrayAdapter<ServiceDetails.Parameter>(this, R.layout.list_item_parameter) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    view = View.inflate(ServiceDetailActivity.this, R.layout.list_item_parameter, null);
                }

                ServiceDetails.Parameter parameter = getItem(position);

                TextView serverKey = (TextView) view.findViewById(R.id.parameter_name);
                serverKey.setText(parameter.name);
                TextView serverAddress = (TextView) view.findViewById(R.id.parameter_values);
                serverAddress.setText(StringUtils.join(parameter.values, '\n'));

                return view;
            }
        };
        ListView parameterList = (ListView) findViewById(R.id.service_parameter_list);
        parameterList.setAdapter(parameterAdapter);

        final QueryTask queryTask = new QueryTask() {
            @Override
            public void onProgressUpdate(ServiceDetails... details) {
                setServiceDetails(details[0]);
            }
        };

        SigningKey key = Identity.getSigningKey();
        QueryTask.QueryParameters parameters = new QueryTask.QueryParameters(key, server, service);
        queryTask.execute(parameters);

        progressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.query_loading),
                true, true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (queryTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    queryTask.cancel();
                    finish();
                }
            }
        });
    }

    private void setServiceDetails(ServiceDetails details) {
        serviceDetails = details;
        parameterAdapter.addAll(details.parameters);

        TextView subtype = (TextView) findViewById(R.id.service_subtype);
        subtype.setText(details.subtype);
        TextView location = (TextView) findViewById(R.id.service_location);
        location.setText(details.location);

        progressDialog.dismiss();
    }

    public void onInvokeClicked(View view) {
        Intent intent = getServiceIntent(serviceDetails.subtype);
        intent.putExtra(EXTRA_SERVICE_DETAILS, serviceDetails);
        startActivity(intent);
    }

    public Intent getServiceIntent(String type) {
        switch (type) {
            case "invoke":
                return new Intent(this, InvokeActivity.class);
            default:
                return new Intent(this, GenericActivity.class);
        }
    }

}
