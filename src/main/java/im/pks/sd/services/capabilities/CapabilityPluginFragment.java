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

package im.pks.sd.services.capabilities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import im.pks.sd.controller.R;
import im.pks.sd.entities.CapabilityRequestTo;
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.services.PluginFragment;

import java.util.Collections;
import java.util.List;

public class CapabilityPluginFragment extends PluginFragment implements View.OnClickListener, CapabilityRequestsTask.RequestListener {

    private Button startButton;

    private RecyclerView cardsView;
    private CapabilityRequestsAdapter cardsAdapter;

    private ServiceDescriptionTo service;
    private CapabilityRequestsTask capabilityRequestsTask;

    public static CapabilityPluginFragment createFragment(ServiceDescriptionTo service) {
        CapabilityPluginFragment fragment = new CapabilityPluginFragment();
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

        cardsAdapter = new CapabilityRequestsAdapter();
        cardsView.setAdapter(cardsAdapter);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        capabilityRequestsTask.cancel();
        capabilityRequestsTask = null;
        startButton.setEnabled(true);
    }

    @Override
    public List<ParameterTo> getParameters() {
        return Collections.singletonList(new ParameterTo("mode", "register"));
    }

    @Override
    public void onClick(View v) {
        capabilityRequestsTask = new CapabilityRequestsTask(service, getParameters());
        capabilityRequestsTask.setRequestListener(this);
        capabilityRequestsTask.execute();
        startButton.setEnabled(false);
    }

    @Override
    public void onRequestReceived(final CapabilityRequestTo request, final Runnable accept) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardsAdapter.addRequest(request, accept);
            }
        });
    }
}

