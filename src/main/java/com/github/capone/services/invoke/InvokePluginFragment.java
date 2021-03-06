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

package com.github.capone.services.invoke;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.github.capone.controller.R;
import com.github.capone.controller.invoke.ServiceChooserDialog;
import com.github.capone.controller.invoke.ServiceParametersDialog;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.services.Plugin;
import com.github.capone.services.PluginFragment;
import com.github.capone.services.Plugins;
import com.google.protobuf.nano.MessageNano;
import nano.Invoke;

public class InvokePluginFragment extends PluginFragment {

    private View view;
    private LinearLayout pluginLayout;

    private Server invokerServer;
    private ServiceDescription invoker;
    private Server serviceServer;
    private ServiceDescription service;
    private MessageNano serviceParameters;

    private Button invokeButton;

    public static InvokePluginFragment createFragment(Server invokerServer,
                                                      ServiceDescription invoker) {
        InvokePluginFragment fragment = new InvokePluginFragment();
        fragment.invokerServer = invokerServer;
        fragment.invoker = invoker;
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_plugin_invoke, container, false);

        pluginLayout = (LinearLayout) view.findViewById(R.id.plugin_layout);

        Button selectButton = (Button) view.findViewById(R.id.button_select_server);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectClicked();
            }
        });

        invokeButton = (Button) view.findViewById(R.id.button_invoke);
        invokeButton.setEnabled(false);
        invokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInvokeClicked();
            }
        });

        return view;
    }

    private void onSelectClicked() {
        ServiceChooserDialog dialog = new ServiceChooserDialog();
        dialog.setOnServiceChosenListener(new ServiceChooserDialog.OnServiceChosenListener() {
            @Override
            public void onServiceChosen(final Server server, final ServiceDescription details) {
                ServiceParametersDialog parametersDialog
                        = ServiceParametersDialog.createDialog(server, details);
                parametersDialog.setOnParametersChosenListener(
                        new ServiceParametersDialog.OnParametersChosenListener() {
                            @Override
                            public void onParametersChosen(MessageNano parameters) {
                                setServiceDetails(server, details, parameters);
                                invokeButton.setEnabled(true);
                            }
                        });
                parametersDialog.show(getFragmentManager(), "ServiceParametersDialog");
            }
        });
        dialog.show(getFragmentManager(), "ServiceChooserDialog");
    }

    private void onInvokeClicked() {
        InvokePluginTask task = new InvokePluginTask(invokerServer, invoker, serviceServer, service,
                                                     serviceParameters) {
            @Override
            protected void onPostExecute(Throwable throwable) {
                if (throwable != null) {
                    Toast.makeText(InvokePluginFragment.this.getActivity(),
                                   throwable.getLocalizedMessage(),
                                   Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();

        Toast.makeText(getActivity(), R.string.service_was_invoked, Toast.LENGTH_SHORT).show();
    }

    private void setServiceDetails(Server server,
                                   ServiceDescription results, MessageNano parameters) {
        this.serviceServer = server;
        this.service = results;
        this.serviceParameters = parameters;

        Plugin plugin = Plugins.getPlugin(results.type);

        if (plugin == null) {
            Toast.makeText(getActivity(),
                           String.format(getString(R.string.no_plugin_for_type), results.type),
                           Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceImage.setImageResource(plugin.getCategoryImageId(service.category));

        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        serverAddress.setText(String.format("%s:%d", server.address, service.port));
        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(server.signatureKey.toString());
        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(service.name);
        TextView serviceType = (TextView) view.findViewById(R.id.service_type);
        serviceType.setText(service.category);

        pluginLayout.setVisibility(View.VISIBLE);
    }

    public Invoke.InvokeParams getParameters() {
        Invoke.InvokeParams params = new Invoke.InvokeParams();
        params.serviceAddress = serviceServer.address;
        params.servicePort = service.port;
        params.serviceIdentity = serviceServer.signatureKey.toMessage();
        params.serviceType = service.type;

        return params;
    }

}
