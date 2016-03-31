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

package im.pks.sd.controller.invoke;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import im.pks.sd.controller.R;
import im.pks.sd.services.PluginFragment;
import im.pks.sd.services.Plugins;

import java.util.List;

public class ServiceParametersDialog extends DialogFragment
        implements View.OnClickListener {

    private PluginFragment fragment;
    private QueryResults queryResults;
    private OnParametersChosenListener listener;

    public interface OnParametersChosenListener {
        void onParametersChosen(List<QueryResults.Parameter> parameters);
    }

    public static ServiceParametersDialog createDialog(QueryResults results) {
        ServiceParametersDialog dialog = new ServiceParametersDialog();
        dialog.queryResults = results;
        return dialog;
    }

    public void setOnParametersChosenListener(OnParametersChosenListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_service_parameters, container);
        Button button = (Button) view.findViewById(R.id.button_ok);
        button.setOnClickListener(this);

        fragment = Plugins.getPlugin(queryResults).getFragment(queryResults);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.plugin_view, fragment);
        transaction.commit();

        return view;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onParametersChosen(fragment.getParameters());
        }
        dismiss();
    }

}
