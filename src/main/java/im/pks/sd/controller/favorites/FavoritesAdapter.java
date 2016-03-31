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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.persistence.Server;

public class FavoritesAdapter extends ArrayAdapter<Server> {

    public FavoritesAdapter(Context context) {
        super(context, R.layout.list_item_favorite);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_item_favorite, null);
        }

        TextView name = (TextView) view.findViewById(R.id.server_name);
        TextView address = (TextView) view.findViewById(R.id.server_address);

        Server server = getItem(position);
        if (server.getName() == null) {
            name.setText(server.getAddress());
            address.setText(null);
        } else {
            name.setText(server.getName());
            address.setText(server.getAddress());
        }

        TextView keyView = (TextView) view.findViewById(R.id.server_key);
        keyView.setText(server.getPublicKey());

        return view;
    }

}
