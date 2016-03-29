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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Server;

import java.util.HashSet;
import java.util.Set;

public class ServerListAdapter extends ArrayAdapter<ServerTo> {

    private Set<ServerTo> servers = new HashSet<>();

    public ServerListAdapter(Context context) {
        super(context, R.layout.list_item_server);
    }

    @Override
    public View getView(final int position, View view, ViewGroup group) {
        return getServerView(position, view);
    }

    private View getServerView(int position, View view) {
        final ServerTo server = getItem(position);

        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_item_server, null);
        }

        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(server.publicKey);
        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        serverAddress.setText(server.address);

        TableRow row = (TableRow) view.findViewById(R.id.server_name_row);

        ImageButton favoriteButton = (ImageButton) view.findViewById(R.id.button_favorite);
        Server favorite = Server.findByNaturalKey(server.publicKey, server.address);
        if (favorite == null) {
            row.setVisibility(View.GONE);
            setAddButton(favoriteButton, server);
        } else {
            TextView serverName = (TextView) view.findViewById(R.id.server_name);
            serverName.setText(favorite.getName());
            row.setVisibility(View.VISIBLE);

            setRemoveButton(favoriteButton, server);
        }

        return view;
    }

    private void setAddButton(final ImageButton button, final ServerTo server) {
        button.setImageResource(R.drawable.favorite_add);
        button.setContentDescription(getContext().getString(R.string.favorite_add));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavoriteAddClicked(server);
                setRemoveButton(button, server);
            }
        });
    }

    private void setRemoveButton(final ImageButton button, final ServerTo server) {
        button.setImageResource(R.drawable.favorite_remove);
        button.setContentDescription(getContext().getString(R.string.favorite_remove));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavoriteRemoveClicked(server);
                setAddButton(button, server);
            }
        });
    }

    private void onFavoriteAddClicked(ServerTo server) {
        Server record = new Server();
        record.setName(server.address);
        record.setAddress(server.address);
        record.setPublicKey(server.publicKey);
        record.save();
    }

    private void onFavoriteRemoveClicked(ServerTo server) {
        Server favorite = Server.findByNaturalKey(server.publicKey, server.address);
        favorite.delete();
    }

    @Override
    public void add(ServerTo server) {
        if (servers.contains(server)) {
            return;
        }

        servers.add(server);
        super.add(server);
    }

}
