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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.github.capone.controller.R;
import com.github.capone.protocol.entities.CapabilityRequest;

import java.text.DateFormat;
import java.util.ArrayList;

public class CapabilitiesRequestsAdapter
        extends RecyclerView.Adapter<CapabilitiesRequestsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView requester;
        private final TextView service;
        private final TextView address;
        private final TextView received;
        private final Button accept;
        private final Button reject;

        ViewHolder(View itemView) {
            super(itemView);
            requester = (TextView) itemView.findViewById(R.id.text_view_requester);
            service = (TextView) itemView.findViewById(R.id.text_view_service);
            address = (TextView) itemView.findViewById(R.id.text_view_address);
            received = (TextView) itemView.findViewById(R.id.text_view_received);
            accept = (Button) itemView.findViewById(R.id.button_accept);
            reject = (Button) itemView.findViewById(R.id.button_reject);
        }
    }

    private final ArrayList<CapabilityRequest> requests = new ArrayList<>();
    private final ArrayList<Runnable> accepts = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                         .inflate(R.layout.card_capability_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CapabilityRequest request = requests.get(position);
        final Runnable acceptor = accepts.get(position);

        holder.requester.setText(request.requesterIdentity.toString());
        holder.service.setText(request.serviceIdentity.toString());
        holder.address.setText(String.format("%s:%s", request.serviceAddress, request.servicePort));

        if (request.received != null) {
            String date = DateFormat.getTimeInstance().format(request.received);
            holder.received.setText(date);
        } else {
            holder.received.setText(null);
        }

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptor.run();
                removeRequest(request);
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRequest(request);
            }
        });
    }

    public void addRequest(CapabilityRequest request, Runnable accept) {
        requests.add(request);
        accepts.add(accept);
        notifyDataSetChanged();
    }

    public void removeRequest(CapabilityRequest request) {
        int index = requests.indexOf(request);
        if (index >= 0) {
            requests.remove(index);
            accepts.remove(index);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

}
