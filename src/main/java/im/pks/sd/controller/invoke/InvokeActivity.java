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

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.QueryTask;
import im.pks.sd.services.Plugin;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.PluginTask;
import im.pks.sd.services.Plugins;
import org.abstractj.kalium.keys.SigningKey;

public class InvokeActivity extends AppCompatActivity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE = "service";

    private ProgressDialog progressDialog;
    private PluginFragment fragment;
    private Plugin plugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        ServerTo server = (ServerTo) intent.getSerializableExtra(EXTRA_SERVER);
        ServiceTo service = (ServiceTo) intent.getSerializableExtra(EXTRA_SERVICE);

        ImageView serviceImage = (ImageView) findViewById(R.id.service_image);
        serviceImage.setImageResource(Plugins.getCategoryImageId(service.category));

        TextView serverAddress = (TextView) findViewById(R.id.server_address);
        serverAddress.setText(String.format("%s:%d", server.address, service.port));

        TextView serviceName = (TextView) findViewById(R.id.service_name);
        serviceName.setText(service.name);

        final QueryTask queryTask = new QueryTask() {
            @Override
            public void onProgressUpdate(QueryResults... details) {
                setServiceDetails(details[0]);
            }
        };

        SigningKey key = Identity.getSigningKey();
        QueryTask.Parameters parameters = new QueryTask.Parameters(key, server, service);
        queryTask.execute(parameters);

        progressDialog = ProgressDialog.show(this, getString(R.string.loading),
                                             getString(R.string.query_loading),
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

    private void setServiceDetails(QueryResults results) {
        TextView type = (TextView) findViewById(R.id.service_type);
        type.setText(results.type);
        TextView location = (TextView) findViewById(R.id.service_location);
        location.setText(results.location);

        plugin = Plugins.getPlugin(results);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragment = plugin.getFragment(results);
        transaction.add(R.id.plugin_view, fragment);
        transaction.commit();

        progressDialog.dismiss();
    }

    public void onInvokeClicked(View view) {
        PluginTask task = plugin.getTask(fragment);
        if (task == null) {
            Toast.makeText(this, R.string.service_handler_not_implemented, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        task.execute();
    }

}
