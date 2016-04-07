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
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.persistence.Server;

import java.util.HashSet;
import java.util.Set;

public class ServerListAdapter extends ArrayAdapter<ServerTo> {

    public interface OnStarClickedListener {
        boolean onStarClicked(ServerTo server);
    }

    private OnStarClickedListener onStarClickedListener;
    private Set<ServerTo> servers = new HashSet<>();

    public ServerListAdapter(Context context) {
        super(context, R.layout.list_item_server);
    }

    @Override
    public View getView(final int position, View view, ViewGroup group) {
        return getServerView(position, view);
    }

    private View getServerView(int position, View view) {
        final ServerTo to = getItem(position);
        Server server = Server.findByTo(to);

        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_item_server, null);
        }

        TextView name = (TextView) view.findViewById(R.id.server_name);
        TextView address = (TextView) view.findViewById(R.id.server_address);
        if (server == null || server.getName() == null) {
            name.setText(to.address);
            address.setText(null);
        } else {
            name.setText(server.getName());
            address.setText(server.getAddress());
        }

        final ImageButton favorite = (ImageButton) view.findViewById(R.id.button_favorite);
        if (server != null) {
            favorite.setImageResource(android.R.drawable.star_on);
        } else {
            favorite.setImageResource(android.R.drawable.star_off);
        }
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStarClickedListener == null) {
                    return;
                }

                boolean starred = onStarClickedListener.onStarClicked(to);
                favorite.setImageResource(starred ? android.R.drawable.star_on
                                                  : android.R.drawable.star_off);
            }
        });

        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(to.publicKey);

        return view;
    }

    public void setOnStarClickedListener(OnStarClickedListener listener) {
        this.onStarClickedListener = listener;
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
