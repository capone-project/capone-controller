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

package im.pks.sd.services.invoke;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.protobuf.nano.MessageNano;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.ServiceChooserDialog;
import im.pks.sd.controller.invoke.ServiceParametersDialog;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.entities.SignatureKeyTo;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.Plugins;
import nano.Invoke;

public class InvokePluginFragment extends PluginFragment {

    private View view;
    private LinearLayout pluginLayout;

    private ServiceDescriptionTo invoker;
    private ServiceDescriptionTo service;
    private MessageNano serviceParameters;

    private Button invokeButton;

    public static InvokePluginFragment createFragment(ServiceDescriptionTo invoker) {
        InvokePluginFragment fragment = new InvokePluginFragment();
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
            public void onServiceChosen(final ServiceDescriptionTo details) {
                ServiceParametersDialog parametersDialog
                        = ServiceParametersDialog.createDialog(details);
                parametersDialog.setOnParametersChosenListener(
                        new ServiceParametersDialog.OnParametersChosenListener() {
                            @Override
                            public void onParametersChosen(MessageNano parameters) {
                                setServiceDetails(details, parameters);
                                invokeButton.setEnabled(true);
                            }
                        });
                parametersDialog.show(getFragmentManager(), "ServiceParametersDialog");
            }
        });
        dialog.show(getFragmentManager(), "ServiceChooserDialog");
    }

    private void onInvokeClicked() {
        InvokePluginTask task = new InvokePluginTask(invoker, service, getParameters(), serviceParameters) {
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

    private void setServiceDetails(ServiceDescriptionTo results, MessageNano parameters) {
        this.service = results;
        this.serviceParameters = parameters;

        ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceImage.setImageResource(Plugins.getCategoryImageId(service.service.category));

        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        serverAddress.setText(String.format("%s:%d", service.server.address, service.service.port));
        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(service.server.publicKey);
        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(service.service.name);
        TextView serviceType = (TextView) view.findViewById(R.id.service_type);
        serviceType.setText(service.service.category);

        pluginLayout.setVisibility(View.VISIBLE);
    }

    public Invoke.InvokeParams getParameters() {
        Invoke.InvokeParams params = new Invoke.InvokeParams();
        params.serviceAddress = service.server.address;
        params.servicePort = Integer.toString(service.service.port);
        params.serviceIdentity = new SignatureKeyTo(service.server.publicKey).toMessage();
        params.serviceType = service.type;
        params.serviceParameters = null;

        return params;
    }

}
