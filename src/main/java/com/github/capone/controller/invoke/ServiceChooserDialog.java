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

package com.github.capone.controller.invoke;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.github.capone.controller.R;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.protocol.DiscoveryTask;
import com.github.capone.controller.discovery.ServerListAdapter;
import com.github.capone.controller.services.ServiceListAdapter;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.Service;
import com.github.capone.persistence.IdentityRecord;
import com.github.capone.protocol.QueryTask;

public class ServiceChooserDialog extends DialogFragment {

    public interface OnServiceChosenListener {
        void onServiceChosen(Server server, ServiceDescription service);
    }

    private OnServiceChosenListener listener;

    private ListView list;

    private DiscoveryTask discovery;
    private QueryTask query;

    public void setOnServiceChosenListener(OnServiceChosenListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_service_chooser, null);
        list = (ListView) view.findViewById(R.id.server_list);

        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                discovery = null;
                showServices(serverAdapter.getItem(position));
            }
        });

        stopTasks();
        discovery = new DiscoveryTask() {
            @Override
            public void onProgressUpdate(Server... server) {
                serverAdapter.add(server[0]);
            }

            @Override
            protected void onPostExecute(Throwable throwable) {
                if (throwable != null) {
                    Toast.makeText(ServiceChooserDialog.this.getActivity(),
                                   throwable.getLocalizedMessage(),
                                   Toast.LENGTH_SHORT).show();
                }
            }
        };
        discovery.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopTasks();
    }

    private void stopTasks() {
        if (discovery != null) {
            discovery.cancel();
            discovery = null;
        }
        if (query != null) {
            query.cancel();
            query = null;
        }
    }

    private void showServices(final Server server) {
        final ServiceListAdapter adapter = new ServiceListAdapter(getActivity());
        adapter.addAll(server.services);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startQuery(server, adapter.getItem(position));
            }
        });
    }

    private void startQuery(final Server server, final Service service) {
        stopTasks();
        query = new QueryTask() {
            @Override
            public void onProgressUpdate(ServiceDescription... description) {
                listener.onServiceChosen(server, description[0]);
                dismiss();
            }
        };
        query.execute(new QueryTask.Parameters(IdentityRecord.getSigningKey(), server, service));
    }

}
