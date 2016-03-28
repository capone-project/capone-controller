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
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetailActivity;
import im.pks.sd.controller.query.ServiceDetails;

public class GenericActivity extends FragmentActivity {

    private ArrayAdapter<ServiceDetails.Parameter> parameterAdapter;

    private ServiceDetails service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_genric);

        service = (ServiceDetails) getIntent().getSerializableExtra(ServiceDetailActivity.EXTRA_SERVICE_DETAILS);

        parameterAdapter = new ArrayAdapter<ServiceDetails.Parameter>(this, R.layout.list_item_editable_parameter) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                if (view == null) {
                    view = View.inflate(GenericActivity.this, R.layout.list_item_editable_parameter, null);
                }

                TextView parameterName = (TextView) view.findViewById(R.id.parameter_name);
                parameterName.setText(getItem(position).name);

                return view;
            }
        };
        parameterAdapter.addAll(service.parameters);

        ListView parameterList = (ListView) findViewById(R.id.service_parameter_list);
        parameterList.setAdapter(parameterAdapter);
    }

    public void onConnectClicked(View view) {
        Toast.makeText(this, R.string.service_handler_not_implemented, Toast.LENGTH_SHORT).show();
    }

}
