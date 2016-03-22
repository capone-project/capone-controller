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

package im.pks.sd.controller.query;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;

public class ServiceQueryActivity extends Activity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE = "service";

    private Server server;
    private Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_query);

        Intent intent = getIntent();
        server = (Server) intent.getSerializableExtra(EXTRA_SERVER);
        service = (Service) intent.getSerializableExtra(EXTRA_SERVICE);

        ImageView serviceImage = (ImageView) findViewById(R.id.service_image);
        serviceImage.setImageResource(service.getResourceId());
        TextView serverKey = (TextView) findViewById(R.id.server_key);
        serverKey.setText(server.publicKey);
        TextView serverAddress = (TextView) findViewById(R.id.server_address);
        serverAddress.setText(server.address);
        TextView serviceName = (TextView) findViewById(R.id.service_name);
        serviceName.setText(service.name);
        TextView servicePort = (TextView) findViewById(R.id.service_port);
        servicePort.setText(service.port);
        TextView serviceType = (TextView) findViewById(R.id.service_type);
        serviceType.setText(service.type);
    }

}
