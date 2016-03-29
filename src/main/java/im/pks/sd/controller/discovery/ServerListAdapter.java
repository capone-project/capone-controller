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
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServerTo;

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

        return view;
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
