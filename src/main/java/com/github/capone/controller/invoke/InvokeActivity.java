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

package com.github.capone.controller.invoke;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.github.capone.controller.R;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.services.ServicePlugin;
import com.github.capone.services.ServicePlugins;

public class InvokeActivity extends AppCompatActivity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE_DESCRIPTION = "servicedescription";

    private Server server;
    private ServiceDescription serviceDescription;
    private ServicePlugin plugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        server = intent.getParcelableExtra(EXTRA_SERVER);
        serviceDescription = intent.getParcelableExtra(EXTRA_SERVICE_DESCRIPTION);

        if ((plugin = ServicePlugins.getPlugin(serviceDescription.type)) == null) {
            Toast.makeText(InvokeActivity.this,
                           String.format(getString(R.string.no_plugin_for_type),
                                         serviceDescription.type),
                           Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadConfigurationFragment(server, serviceDescription, plugin);
    }

    private void loadConfigurationFragment(Server server, ServiceDescription results, ServicePlugin plugin) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.plugin_view, this.plugin.getConfigurationFragment(server, results));
        transaction.commit();
    }

}
