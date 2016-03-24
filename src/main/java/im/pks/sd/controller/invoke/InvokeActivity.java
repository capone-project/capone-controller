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

package im.pks.sd.controller.invoke;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;
import im.pks.sd.controller.query.ServiceDetailActivity;
import im.pks.sd.controller.query.ServiceDetails;

public class InvokeActivity extends FragmentActivity {

    private static final int SERVICE_SELECTION_REQUEST_CODE = 1;

    private ServiceDetails service;
    private Server invocationServer;
    private Service invocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_invoke);

        service = (ServiceDetails) getIntent().getSerializableExtra(ServiceDetailActivity.EXTRA_SERVICE_DETAILS);
    }

    public void onInvocationServerSelectionClicked(View view) {
        Intent intent = new Intent(this, ServiceChooserActivity.class);
        startActivityForResult(intent, SERVICE_SELECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SERVICE_SELECTION_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return;
        }

        invocationServer = (Server) data.getSerializableExtra(ServiceChooserActivity.EXTRA_SELECTED_SERVER);
        TextView serverKey = (TextView) findViewById(R.id.server_key);
        serverKey.setText(invocationServer.publicKey);
        TextView serverAddress = (TextView) findViewById(R.id.server_address);
        serverAddress.setText(invocationServer.address);

        invocationService = (Service) data.getSerializableExtra(ServiceChooserActivity.EXTRA_SELECTED_SERVICE);
        ImageView serviceImage = (ImageView) findViewById(R.id.service_image);
        serviceImage.setImageResource(invocationService.getResourceId());
        TextView serviceName = (TextView) findViewById(R.id.service_name);
        serviceName.setText(invocationService.name);
        TextView serviceType = (TextView) findViewById(R.id.service_type);
        serviceType.setText(invocationService.type);
        TextView servicePort = (TextView) findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(invocationService.port));
    }

    public void onConnectClicked(View view) {

    }

}
