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

package im.pks.sd.controller.favorites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.DiscoveryTask;
import im.pks.sd.controller.discovery.ServerDetailActivity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.persistence.Server;

import java.util.List;

public class FavoritesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        final FavoritesAdapter adapter = new FavoritesAdapter(this);
        List<Server> servers = Server.listAll(Server.class);
        adapter.addAll(servers);

        ListView listView = (ListView) findViewById(R.id.favorites_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onServerClicked(adapter.getItem(position));
            }
        });
    }

    private void onServerClicked(Server server) {
        DiscoveryTask discovery =
                new DiscoveryTask(server, Identity.getSigningKey().getVerifyKey()) {
                    @Override
                    public void onProgressUpdate(ServerTo... server) {
                        cancel();

                        Intent intent = new Intent(FavoritesActivity.this,
                                                   ServerDetailActivity.class);
                        intent.putExtra(ServerDetailActivity.EXTRA_SERVER, server[0]);
                        startActivity(intent);
                    }
                };
        discovery.execute();
    }

}
