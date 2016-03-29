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

package im.pks.sd.controller.discovery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.services.Plugins;

public abstract class ServiceListAdapter extends ArrayAdapter<ServiceTo> {

    public ServiceListAdapter(Context context) {
        super(context, R.layout.list_item_service);
    }

    @Override
    public View getView(final int position, View view, ViewGroup group) {
        final ServiceTo service = getItem(position);

        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_item_service, null);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onServiceClicked(service);
            }
        });

        ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceImage.setImageResource(Plugins.getCategoryImageId(service.category));

        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(service.name);
        TextView serviceType = (TextView) view.findViewById(R.id.service_type);
        serviceType.setText(service.category);
        TextView servicePort = (TextView) view.findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(service.port));

        return view;
    }

    public abstract void onServiceClicked(ServiceTo service);

}
