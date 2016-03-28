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

package im.pks.sd.controller.discovery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetailActivity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;

public class ServerDetailActivity extends Activity {

    public static final String EXTRA_SERVER = "server";

    private ServerTo server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);

        server = (ServerTo) getIntent().getSerializableExtra(EXTRA_SERVER);

        TextView keyView = (TextView) findViewById(R.id.server_key);
        keyView.setText(server.publicKey);
        TextView addressView = (TextView) findViewById(R.id.server_address);
        addressView.setText(server.address);

        ListView serviceList = (ListView) findViewById(R.id.service_list);
        ServiceListAdapter adapter = new ServiceListAdapter(this) {
            @Override
            public void onServiceClicked(ServiceTo service) {
                Intent intent = new Intent(ServerDetailActivity.this,
                                                  ServiceDetailActivity.class);
                intent.putExtra(ServiceDetailActivity.EXTRA_SERVICE, service);
                intent.putExtra(ServiceDetailActivity.EXTRA_SERVER, server);
                startActivity(intent);
            }
        };
        adapter.addAll(server.services);
        serviceList.setAdapter(adapter);
    }

}
