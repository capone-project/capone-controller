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

package im.pks.sd.controller.invoke;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.DiscoveryTask;
import im.pks.sd.controller.discovery.ServerListAdapter;
import im.pks.sd.controller.services.ServiceListAdapter;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.QueryTask;

public abstract class ServiceChooserDialog extends DialogFragment {

    private ListView list;

    private DiscoveryTask discovery;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_service_chooser, null);
        list = (ListView) view.findViewById(R.id.server_list);

        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discovery.cancel();
                dismiss();
            }
        });

        startDiscovery();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    private void startDiscovery() {
        final ServerListAdapter serverAdapter = new ServerListAdapter(getActivity());
        list.setAdapter(serverAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                discovery.cancel();
                showServices(serverAdapter.getItem(position));
            }
        });

        discovery = new DiscoveryTask(Identity.getSigningKey().getVerifyKey()) {
            @Override
            public void onProgressUpdate(ServerTo... server) {
                serverAdapter.add(server[0]);
            }
        };
        discovery.execute();
    }

    private void showServices(final ServerTo server) {
        final ServiceListAdapter adapter = new ServiceListAdapter(getActivity());
        adapter.addAll(server.services);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                query(server, adapter.getItem(position));
                dismiss();
            }
        });
    }

    private void query(final ServerTo server, final ServiceTo service) {
        QueryTask task = new QueryTask() {
            @Override
            public void onProgressUpdate(QueryResults... details) {
                onServiceChosen(details[0]);
            }
        };
        task.execute(new QueryTask.Parameters(Identity.getSigningKey(), server, service));
    }

    public abstract void onServiceChosen(QueryResults details);

}
