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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.DiscoveryTask;
import im.pks.sd.controller.services.ServiceListActivity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.persistence.Server;

import java.util.List;

public class FavoritesActivity extends Activity {

    private FavoritesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        adapter = new FavoritesAdapter(this);
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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onServerLongClicked(adapter.getItem(position));
                return true;
            }
        });
    }

    private void onServerLongClicked(final Server server) {
        startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.favorites, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        onEditClicked(server);
                        mode.finish();
                        return true;
                    case R.id.remove:
                        onRemoveClicked(server);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void onRemoveClicked(final Server server) {
        server.delete();
        adapter.remove(server);
    }

    private void onEditClicked(final Server server) {
        final EditText name = new EditText(this);

        new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        server.setName(name.getText().toString());
                        server.save();
                        adapter.notifyDataSetInvalidated();
                    }
                })
                .setView(name)
                .setTitle(R.string.title_choose_server_name)
                .create().show();
    }

    private void onServerClicked(Server server) {
        DiscoveryTask discovery =
                new DiscoveryTask(server, Identity.getSigningKey().getVerifyKey()) {
                    @Override
                    public void onProgressUpdate(ServerTo... server) {
                        cancel();

                        Intent intent = new Intent(FavoritesActivity.this,
                                                   ServiceListActivity.class);
                        intent.putExtra(ServiceListActivity.EXTRA_SERVER, server[0]);
                        startActivity(intent);
                    }
                };
        discovery.execute();
    }

}
