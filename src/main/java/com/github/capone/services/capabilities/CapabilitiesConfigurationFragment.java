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

package com.github.capone.services.capabilities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.github.capone.controller.R;
import com.github.capone.protocol.entities.CapabilityRequest;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.services.ServiceConfigurationFragment;
import nano.Capabilities;

public class CapabilitiesConfigurationFragment extends ServiceConfigurationFragment implements View.OnClickListener, CapabilitiesRequestsTask.RequestListener {

    private Button startButton;

    private RecyclerView cardsView;
    private CapabilitiesRequestsAdapter cardsAdapter;

    private Server server;
    private ServiceDescription service;
    private CapabilitiesRequestsTask capabilitiesRequestsTask;

    public static CapabilitiesConfigurationFragment createFragment(Server server,
                                                                   ServiceDescription service) {
        CapabilitiesConfigurationFragment fragment = new CapabilitiesConfigurationFragment();
        fragment.server = server;
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_capability, container, false);

        startButton = (Button) view.findViewById(R.id.button_start);
        startButton.setOnClickListener(this);

        cardsView = (RecyclerView) view.findViewById(R.id.request_cards);
        cardsView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        cardsView.setLayoutManager(layoutManager);

        cardsAdapter = new CapabilitiesRequestsAdapter();
        cardsView.setAdapter(cardsAdapter);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (capabilitiesRequestsTask != null) {
            capabilitiesRequestsTask.cancel();
            capabilitiesRequestsTask = null;
        }

        startButton.setEnabled(true);
    }

    @Override
    public Capabilities.CapabilitiesParams getParameters() {
        Capabilities.CapabilitiesParams params = new Capabilities.CapabilitiesParams();
        params.type = Capabilities.CapabilitiesParams.REGISTER;
        return params;
    }

    @Override
    public void onClick(View v) {
        capabilitiesRequestsTask = new CapabilitiesRequestsTask(server, service, getParameters());
        capabilitiesRequestsTask.setRequestListener(this);
        capabilitiesRequestsTask.execute();
        startButton.setEnabled(false);
    }

    @Override
    public void onRequestReceived(final CapabilityRequest request, final Runnable accept) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardsAdapter.addRequest(request, accept);
            }
        });
    }
}

