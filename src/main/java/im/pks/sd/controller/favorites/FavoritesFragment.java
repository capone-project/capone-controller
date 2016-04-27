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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import im.pks.sd.controller.R;
import im.pks.sd.controller.services.ServiceListActivity;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.persistence.Server;
import im.pks.sd.protocol.DirectedDiscoveryTask;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.util.List;

public class FavoritesFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    private FavoritesAdapter adapter;
    private DirectedDiscoveryTask discovery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_list, container, false);

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.button_add);
        button.setOnClickListener(this);

        adapter = new FavoritesAdapter(getActivity());
        List<Server> servers = Server.listAll(Server.class);
        adapter.addAll(servers);

        ListView list = (ListView) view.findViewById(R.id.favorites_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText name = new EditText(getActivity());
        name.setHint(R.string.server_name);
        final EditText address = new EditText(getActivity());
        address.setHint(R.string.server_address);
        final EditText publicKey = new EditText(getActivity());
        publicKey.setHint(R.string.public_key);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name, params);
        layout.addView(address, params);
        layout.addView(publicKey, params);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_add_favorite)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addServer(name.getText().toString(),
                                  address.getText().toString(),
                                  publicKey.getText().toString());
                    }
                })
                .setView(layout)
                .show();
    }

    private void addServer(String name, String address, String publicKey) {
        try {
            VerifyKey key = new VerifyKey(publicKey, Encoder.HEX);
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.invalid_key, Toast.LENGTH_LONG).show();
            return;
        }

        if (address == null || address.isEmpty()) {
            Toast.makeText(getActivity(), R.string.invalid_address, Toast.LENGTH_LONG).show();
            return;
        }

        if (name != null && name.isEmpty()) {
            name = null;
        }

        Server server = new Server();
        server.setName(name);
        server.setAddress(address);
        server.setPublicKey(publicKey);
        server.save();

        adapter.add(server);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Server server = adapter.getItem(position);

        if (discovery != null) {
            discovery.cancel();
        }

        discovery =
                new DirectedDiscoveryTask(Identity.getSigningKey(), server) {
                    @Override
                    protected void onPostExecute(ServerTo server) {
                        Intent intent = new Intent(getActivity(), ServiceListActivity.class);
                        intent.putExtra(ServiceListActivity.EXTRA_SERVER, server);
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

