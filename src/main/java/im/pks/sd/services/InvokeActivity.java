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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.ServiceChooserActivity;
import im.pks.sd.controller.query.ServiceDetailActivity;
import im.pks.sd.controller.query.ServiceDetails;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.ConnectTask;
import im.pks.sd.protocol.RequestTask;
import org.abstractj.kalium.crypto.SecretBox;
import org.abstractj.kalium.encoders.Encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvokeActivity extends FragmentActivity {

    private static final int SERVICE_SELECTION_REQUEST_CODE = 1;

    private ServiceDetails service;
    private ServiceDetails invocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_invoke);

        service = (ServiceDetails) getIntent().getSerializableExtra(ServiceDetailActivity.EXTRA_SERVICE_DETAILS);
    }

    public void onInvocationServerSelectionClicked(View view) {
        Intent intent = new Intent(this, ServiceChooserActivity.class);
        startActivityForResult(intent, SERVICE_SELECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SERVICE_SELECTION_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return;
        }

        invocationService = (ServiceDetails) data.getSerializableExtra(ServiceChooserActivity.EXTRA_SELECTED_SERVICE);

        TextView serverKey = (TextView) findViewById(R.id.server_key);
        serverKey.setText(invocationService.server.publicKey);
        TextView serverAddress = (TextView) findViewById(R.id.server_address);
        serverAddress.setText(invocationService.server.address);

        ImageView serviceImage = (ImageView) findViewById(R.id.service_image);
        serviceImage.setImageResource(Services.getImageId(invocationService.service.type));
        TextView serviceName = (TextView) findViewById(R.id.service_name);
        serviceName.setText(invocationService.service.name);
        TextView serviceType = (TextView) findViewById(R.id.service_type);
        serviceType.setText(invocationService.service.type);
        TextView servicePort = (TextView) findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(invocationService.service.port));
    }

    public void onConnectClicked(View view) {
        RequestTask invocationServiceRequest = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                onSessionInitiated(session);
            }
        };

        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        /* TODO: fill parameters */

        RequestTask.RequestParameters request = new RequestTask.RequestParameters(
                Identity.getSigningKey(), invocationService, parameters);
        invocationServiceRequest.execute(request);
    }

    private void onSessionInitiated(RequestTask.Session session) {
        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceDetails.Parameter("service-identity", invocationService.server.publicKey));
        parameters.add(new ServiceDetails.Parameter("service-address", invocationService.server.address));
        parameters.add(new ServiceDetails.Parameter("service-port", String.valueOf(invocationService.service.port)));
        parameters.add(new ServiceDetails.Parameter("service-type", invocationService.subtype));
        parameters.add(new ServiceDetails.Parameter("sessionid", Integer.toString(session.sessionId)));
        parameters.add(new ServiceDetails.Parameter("sessionkey", Encoder.HEX.encode(session.key)));

        /* TODO: fill parameters */
        parameters.add(new ServiceDetails.Parameter("service-args", "--port"));
        parameters.add(new ServiceDetails.Parameter("service-args", "9999"));

        RequestTask invocationServiceRequest = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                onInvocationSessionInitiated(session);
            }
        };

        RequestTask.RequestParameters request = new RequestTask.RequestParameters(
                Identity.getSigningKey(), service, parameters);
        invocationServiceRequest.execute(request);
    }

    private void onInvocationSessionInitiated(RequestTask.Session session) {
        ConnectTask connectTask = new ConnectTask() {
            @Override
            public void handleConnection(Channel channel) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        };

        ConnectTask.Parameters connectParameter = new ConnectTask.Parameters(
                session.sessionId,
                new SecretBox(session.key),
                service.server,
                service.service);
        connectTask.execute(connectParameter);
    }

}
