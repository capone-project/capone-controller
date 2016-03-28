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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetails;

import static android.view.View.inflate;

public class GenericPluginFragment extends PluginFragment {

    private ServiceDetails service;

    public static GenericPluginFragment createFragment(ServiceDetails service) {
        GenericPluginFragment fragment = new GenericPluginFragment();
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_generic, container, false);

        ArrayAdapter<ServiceDetails.Parameter> parameterAdapter = new ArrayAdapter<ServiceDetails.Parameter>(container.getContext(), R.layout.list_item_editable_parameter) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    view = inflate(GenericPluginFragment.this.getActivity(), R.layout.list_item_editable_parameter, null);
                }

                TextView parameterName = (TextView) view.findViewById(R.id.parameter_name);
                parameterName.setText(getItem(position).name);

                return view;
            }
        };
        parameterAdapter.addAll(service.parameters);

        ListView parameterList = (ListView) view.findViewById(R.id.service_parameter_list);
        parameterList.setAdapter(parameterAdapter);

        return view;
    }

}
