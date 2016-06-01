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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServiceDescriptionTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.inflate;

public class GenericPluginFragment extends PluginFragment {

    private ServiceDescriptionTo service;
    private Map<String, String> parameters;

    public static GenericPluginFragment createFragment(ServiceDescriptionTo service) {
        GenericPluginFragment fragment = new GenericPluginFragment();
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_generic, container, false);

        parameters = new HashMap<>();

        ArrayAdapter<ParameterTo> parameterAdapter = new ArrayAdapter<ParameterTo>(container.getContext(),
                                                                                   R.layout.list_item_editable_parameter) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    view = inflate(GenericPluginFragment.this.getActivity(),
                                   R.layout.list_item_editable_parameter, null);
                }

                final String name = getItem(position).name;
                TextView nameEdit = (TextView) view.findViewById(R.id.parameter_name);
                nameEdit.setText(getItem(position).name);

                EditText valueEdit = (EditText) view.findViewById(R.id.parameter_values);
                valueEdit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        parameters.put(name, s.toString());
                    }
                });
                parameters.put(name, null);

                return view;
            }
        };
        parameterAdapter.addAll(service.parameters);

        ListView parameterList = (ListView) view.findViewById(R.id.service_parameter_list);
        parameterList.setAdapter(parameterAdapter);

        return view;
    }

    public List<ParameterTo> getParameters() {
        List<ParameterTo> result = new ArrayList<>();

        for (Map.Entry<String, String> edit : parameters.entrySet()) {
            ParameterTo parameter = new ParameterTo(edit.getKey(), edit.getValue());
            result.add(parameter);
        }

        return result;
    }
}
