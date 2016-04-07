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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.services.ServiceListActivity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.persistence.Server;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryListFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private List<ServerTo> servers;
    private DiscoveryTask serviceLoader;
    private ServerListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());

        servers = new ArrayList<>();
        adapter = new ServerListAdapter(getActivity());
        adapter.setOnStarClickedListener(new ServerListAdapter.OnStarClickedListener() {
            @Override
            public boolean onStarClicked(ServerTo to) {
                Server server = Server.findByTo(to);
                if (server == null) {
                    server = new Server(to);
                    server.save();
                    return true;
                } else {
                    server.delete();
                    return false;
                }
            }
        });

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ServiceListActivity.class);
        intent.putExtra(ServiceListActivity.EXTRA_SERVER, adapter.getItem(position));
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final ServerTo to = adapter.getItem(position);

        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.discovery, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Server server = Server.findByTo(to);
                if (server == null) {
                    menu.findItem(R.id.remove).setVisible(false);
                } else {
                    menu.findItem(R.id.add).setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add:
                        onAddClicked(to);
                        mode.finish();
                        return true;
                    case R.id.remove:
                        onRemoveClicked(to);
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

    private void onAddClicked(ServerTo to) {
        new Server(to).save();
    }

    private void onRemoveClicked(ServerTo server) {
        Server.findByTo(server).delete();
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    public void startDiscovery() {
        serviceLoader = new DiscoveryTask(servers, Identity.getSigningKey().getVerifyKey()) {
            @Override
            public void onProgressUpdate(ServerTo... server) {
                adapter.add(server[0]);
            }
        };
        serviceLoader.execute();
    }

    public void stopDiscovery() {
        if (serviceLoader != null) {
            serviceLoader.cancel();
            serviceLoader = null;
        }
    }

}
