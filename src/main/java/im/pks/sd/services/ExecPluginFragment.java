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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.QueryResults;

import java.util.ArrayList;
import java.util.List;

public class ExecPluginFragment extends PluginFragment {

    private QueryResults service;

    private EditText executable;

    private ArrayAdapter<String> parametersAdapter;
    private ArrayAdapter<EnvironmentVariable> environmentAdapter;

    private class EnvironmentVariable {
        String variable;
        String value;

        EnvironmentVariable(String variable, String value) {
            this.variable = variable;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", variable, value);
        }
    }

    public static ExecPluginFragment createFragment(QueryResults service) {
        ExecPluginFragment fragment = new ExecPluginFragment();
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_exec, container, false);

        executable = (EditText) view.findViewById(R.id.edit_executable);

        ListView parametersList = (ListView) view.findViewById(R.id.parameters_list);
        parametersAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        parametersList.setAdapter(parametersAdapter);

        ListView environmentList = (ListView) view.findViewById(R.id.environments_list);
        environmentAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        environmentList.setAdapter(environmentAdapter);

        Button invokeButton = (Button) view.findViewById(R.id.button_invoke);
        invokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInvokeClicked();
            }
        });

        view.findViewById(R.id.button_add_parameter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddParameterClicked();
            }
        });
        view.findViewById(R.id.button_add_environment).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddEnvironmentClicked();
                    }
                });

        return view;
    }

    private void onInvokeClicked() {
        if (executable.getText().length() == 0) {
            Toast.makeText(getActivity(), R.string.toast_executable_not_set, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        return;
    }

    private void onAddParameterClicked() {
        final EditText parameterEdit = new EditText(getActivity());
        parameterEdit.setHint(R.string.parameter);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_add_parameter)
                .setView(parameterEdit)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String parameter = parameterEdit.getText().toString().trim();
                        if (!parameter.isEmpty()) {
                            parametersAdapter.add(parameter);
                            parametersAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onAddEnvironmentClicked() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText variableEdit = new EditText(getActivity());
        variableEdit.setHint(R.string.environment_variable);
        final EditText valueEdit = new EditText(getActivity());
        valueEdit.setHint(R.string.environment_value);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(variableEdit, params);
        layout.addView(valueEdit, params);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_add_environment)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String variable = variableEdit.getText().toString().trim();
                        String value = valueEdit.getText().toString().trim();

                        if (!variable.isEmpty() && !value.isEmpty()) {
                            environmentAdapter.add(new EnvironmentVariable(variable, value));
                        }
                    }
                })
                .show();
    }

    @Override
    public List<QueryResults.Parameter> getParameters() {
        List<QueryResults.Parameter> parameters = new ArrayList<>();

        parameters.add(new QueryResults.Parameter("command",
                                                  executable.getText().toString().trim()));

        for (int i = 0; i < parametersAdapter.getCount(); i++) {
            String parameter = parametersAdapter.getItem(i);
            parameters.add(new QueryResults.Parameter("arg", parameter));
        }

        for (int i = 0; i < environmentAdapter.getCount(); i++) {
            EnvironmentVariable variable = environmentAdapter.getItem(i);
            parameters.add(new QueryResults.Parameter("env", variable.toString()));
        }

        return parameters;
    }

}
