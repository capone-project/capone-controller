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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import im.pks.sd.controller.R;
import im.pks.sd.entities.Identity;
import im.pks.sd.entities.ServerTo;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryListActivity extends ListActivity {

    private List<ServerTo> servers;
    private DiscoveryTask serviceLoader;
    private ServerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_list);

        servers = new ArrayList<>();
        adapter = new ServerListAdapter(this) {
            @Override
            public void onServerClicked(ServerTo server) {
                Intent intent = new Intent(DiscoveryListActivity.this, ServerDetailActivity.class);
                intent.putExtra(ServerDetailActivity.EXTRA_SERVER, server);
                startActivity(intent);
            }
        };
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        serviceLoader = new DiscoveryTask(servers, Identity.getSigningKey().getVerifyKey()) {
            @Override
            public void onProgressUpdate(ServerTo... server) {
                adapter.add(server[0]);
            }
        };
        serviceLoader.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        serviceLoader.cancel();
    }

}
