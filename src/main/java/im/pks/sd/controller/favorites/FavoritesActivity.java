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
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.persistence.Server;

import java.util.List;

public class FavoritesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        ArrayAdapter<Server> adapter = new ArrayAdapter<Server>(this, R.layout.list_item_favorite) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.list_item_favorite, null);
                }

                Server server = getItem(position);

                TextView name = (TextView) view.findViewById(R.id.server_name);
                name.setText(server.getName());
                TextView address = (TextView) view.findViewById(R.id.server_address);
                address.setText(server.getAddress());
                TextView key = (TextView) view.findViewById(R.id.server_key);
                key.setText(server.getPublicKey());

                return view;
            }
        };

        List<Server> servers = Server.listAll(Server.class);
        adapter.addAll(servers);

        ListView listView = (ListView) findViewById(R.id.favorites_list);
        listView.setAdapter(adapter);
    }

}
