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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class FavoritesFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private FavoritesAdapter adapter;
    private DiscoveryTask discovery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());

        adapter = new FavoritesAdapter(getActivity());
        List<Server> servers = Server.listAll(Server.class);
        adapter.addAll(servers);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Server server = adapter.getItem(position);

        if (discovery != null) {
            discovery.cancel();
        }

        discovery =
                new DiscoveryTask(server, Identity.getSigningKey().getVerifyKey()) {
                    @Override
                    public void onProgressUpdate(ServerTo... server) {
                        cancel();

                        Intent intent = new Intent(getActivity(), ServiceListActivity.class);
                        intent.putExtra(ServiceListActivity.EXTRA_SERVER, server[0]);
                        startActivity(intent);
                    }
                };
        discovery.execute();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Server server = adapter.getItem(position);

        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = getActivity().getMenuInflater();
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
        return true;
    }

    private void onRemoveClicked(final Server server) {
        server.delete();
        adapter.remove(server);
    }

    private void onEditClicked(final Server server) {
        final EditText name = new EditText(getActivity());

        new AlertDialog.Builder(getActivity())
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

    public void notifyDataSetChanged() {
        adapter.clear();
        List<Server> servers = Server.listAll(Server.class);
        adapter.addAll(servers);
    }

    public void stopDiscovery() {
        if (discovery != null) {
            discovery.cancel();
            discovery = null;
        }
    }

}

