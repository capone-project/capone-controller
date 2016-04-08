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

package im.pks.sd.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.QueryResults;
import im.pks.sd.controller.invoke.ServiceChooserDialog;
import im.pks.sd.controller.invoke.ServiceParametersDialog;

import java.util.ArrayList;
import java.util.List;

public class InvokePluginFragment extends PluginFragment
        implements View.OnClickListener {

    private View view;
    private LinearLayout pluginLayout;

    private QueryResults invoker;
    private QueryResults service;
    private List<QueryResults.Parameter> serviceParameters;

    public static InvokePluginFragment createFragment(QueryResults invoker) {
        InvokePluginFragment fragment = new InvokePluginFragment();
        fragment.invoker = invoker;
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_plugin_invoke, container, false);

        pluginLayout = (LinearLayout) view.findViewById(R.id.plugin_layout);

        Button button = (Button) view.findViewById(R.id.button_select_server);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        ServiceChooserDialog dialog = new ServiceChooserDialog() {
            @Override
            public void onServiceChosen(final QueryResults details) {
                ServiceParametersDialog parametersDialog
                        = ServiceParametersDialog.createDialog(details);
                parametersDialog.setOnParametersChosenListener(
                        new ServiceParametersDialog.OnParametersChosenListener() {
                            @Override
                            public void onParametersChosen(List<QueryResults.Parameter> parameters) {
                                setServiceDetails(details, parameters);
                            }
                        });
                parametersDialog.show(getFragmentManager(), "ServiceParametersDialog");
            }
        };
        dialog.show(getFragmentManager(), "ServiceChooserDialog");
    }

    private void setServiceDetails(QueryResults results, List<QueryResults.Parameter> parameters) {
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

    public List<QueryResults.Parameter> getParameters() {
        List<QueryResults.Parameter> parameters = new ArrayList<>();
        parameters.add(new QueryResults.Parameter("service-identity",
                service.server.publicKey));
        parameters.add(new QueryResults.Parameter("service-address",
                service.server.address));
        parameters.add(new QueryResults.Parameter("service-port",
                String.valueOf(
                        service.service.port)));
        parameters.add(new QueryResults.Parameter("service-type",
                service.type));

        for (QueryResults.Parameter parameter : serviceParameters) {
            parameters.add(new QueryResults.Parameter("service-args", parameter.name));
            parameters.add(new QueryResults.Parameter("service-args", parameter.values));
        }

        return parameters;
    }

    public PluginTask createTask() {
        return new InvokePluginTask(invoker, service, getParameters());
    }

}
