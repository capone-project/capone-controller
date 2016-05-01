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
import android.widget.Toast;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.QueryTask;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.Plugins;
import org.abstractj.kalium.keys.SigningKey;

public class InvokeActivity extends AppCompatActivity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE = "service";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        ServerTo server = intent.getParcelableExtra(EXTRA_SERVER);
        ServiceTo service = intent.getParcelableExtra(EXTRA_SERVICE);

        final QueryTask queryTask = new QueryTask() {
            @Override
            public void onProgressUpdate(ServiceDescriptionTo... description) {
                setServiceDescription(description[0]);
            }

            @Override
            protected void onPostExecute(Throwable throwable) {
                if (throwable != null) {
                    Toast.makeText(InvokeActivity.this,
                                   throwable.getLocalizedMessage(),
                                   Toast.LENGTH_SHORT).show();
                    finish();
                }
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

    private void setServiceDescription(ServiceDescriptionTo results) {
        PluginFragment plugin = Plugins.getPlugin(results);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.plugin_view, plugin);
        transaction.commit();

        progressDialog.dismiss();
    }

}
