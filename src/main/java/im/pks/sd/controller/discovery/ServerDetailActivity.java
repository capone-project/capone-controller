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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetailActivity;
import im.pks.sd.entities.ServerTo;

public class ServerDetailActivity extends Activity {

    public static final String EXTRA_SERVER = "server";

    private ServerTo server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);

        server = (ServerTo) getIntent().getSerializableExtra(EXTRA_SERVER);

        final ServiceListAdapter adapter = new ServiceListAdapter(this);
        adapter.addAll(server.services);

        ListView serviceList = (ListView) findViewById(R.id.service_list);
        serviceList.setAdapter(adapter);
        serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ServerDetailActivity.this,
                        ServiceDetailActivity.class);
                intent.putExtra(ServiceDetailActivity.EXTRA_SERVICE, adapter.getItem(position));
                intent.putExtra(ServiceDetailActivity.EXTRA_SERVER, server);
                startActivity(intent);
            }
        });
    }

}
