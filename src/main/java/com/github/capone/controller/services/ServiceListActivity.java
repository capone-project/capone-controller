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

package com.github.capone.controller.services;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.github.capone.controller.R;
import com.github.capone.controller.invoke.InvokeActivity;
import com.github.capone.persistence.SigningKeyRecord;
import com.github.capone.protocol.QueryTask;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.Service;
import com.github.capone.services.ServicePlugins;

public class ServiceListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    public static final String EXTRA_SERVER = "server";

    private Server server;
    private ServiceListAdapter adapter;
    private QueryTask query;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        server = getIntent().getParcelableExtra(EXTRA_SERVER);

        ListView serviceList = (ListView) findViewById(R.id.service_list);
        adapter = new ServiceListAdapter(this);
        adapter.addAll(server.services);
        serviceList.setAdapter(adapter);
        serviceList.setOnItemClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        stopQuery();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startQuery(adapter.getItem(position));
    }

    private void startQuery(Service service) {
        SigningKey key = SigningKeyRecord.getSigningKey();
        QueryTask.Parameters parameters = new QueryTask.Parameters(key, server, service);

        query = new QueryTask() {
            @Override
            protected void onPostExecute(Result result) {
                progressDialog.dismiss();
                progressDialog = null;

                if (result.error != null) {
                    Toast.makeText(ServiceListActivity.this,
                                   result.error.getLocalizedMessage(),
                                   Toast.LENGTH_LONG).show();
                    return;
                } else if (ServicePlugins.getPlugin(result.description.type) == null) {
                    Toast.makeText(ServiceListActivity.this,
                                   R.string.service_handler_not_implemented,
                                   Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(ServiceListActivity.this,
                                           InvokeActivity.class);
                intent.putExtra(InvokeActivity.EXTRA_SERVER, server);
                intent.putExtra(InvokeActivity.EXTRA_SERVICE_DESCRIPTION, result.description);
                startActivity(intent);
            }
        };

        query.execute(parameters);
        progressDialog = ProgressDialog.show(this, getString(R.string.loading),
                                             getString(R.string.query_loading),
                                             true, true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                stopQuery();
            }
        });
    }

    private void stopQuery() {
        if (query != null) {
            query.cancel();
            query = null;
        }
    }

}
