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

package im.pks.sd.services.capabilities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.ConnectTask;
import im.pks.sd.protocol.RequestTask;
import im.pks.sd.protocol.SessionTask;
import im.pks.sd.services.PluginFragment;
import nano.Capabilities;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.VerifyKey;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CapabilityPluginFragment extends PluginFragment implements View.OnClickListener,
                                                                                ConnectTask.Handler {

    private ServiceDescriptionTo service;

    public static CapabilityPluginFragment createFragment(ServiceDescriptionTo service) {
        CapabilityPluginFragment fragment = new CapabilityPluginFragment();
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_capability, container, false);
        Button invoke = (Button) view.findViewById(R.id.button_invoke);
        invoke.setOnClickListener(this);
        return view;
    }

    @Override
    public List<ServiceDescriptionTo.Parameter> getParameters() {
        return Collections.singletonList(new ServiceDescriptionTo.Parameter("mode", "register"));
    }

    @Override
    public void onClick(View v) {
        new SessionTask(service, getParameters(), this).execute();
    }

    @Override
    public void handleConnection(final Channel channel) {
        final Capabilities.CapabilityRequest request = new Capabilities.CapabilityRequest();

        try {
            while (channel.readProtobuf(request) != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRequest(channel, request);
                    }
                });
            }
        } catch (IOException e) {
            // TODO
            return;
        }
    }

    private void showRequest(final Channel channel, final Capabilities.CapabilityRequest request) {
        String parameters = StringUtils.join(request.parameters, ", ");

        String message = getResources().getString(R.string.capability_request,
                                                  Hex.HEX.encode(request.serviceIdentity),
                                                  Hex.HEX.encode(request.requesterIdentity),
                                                  Hex.HEX.encode(request.invokerIdentity),
                                                  parameters);

        new AlertDialog.Builder(getActivity())
                .setTitle("Request incoming")
                .setMessage(message)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        serviceRequest(channel, request);
                    }
                })
                .show();
    }

    private void serviceRequest(final Channel channel,
                                final Capabilities.CapabilityRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestTask task = new RequestTask(new VerifyKey(request.invokerIdentity),
                                                   new VerifyKey(request.serviceIdentity),
                                                   request.serviceAddress,
                                                   Integer.valueOf(request.servicePort), null);
                RequestTask.Session session;
                try {
                    session = task.requestSession();
                } catch (IOException | VerifyKey.SignatureException e) {
                    return;
                }

                Capabilities.Capability capability = new Capabilities.Capability();
                capability.identity = request.invokerIdentity;
                capability.sessionid = session.sessionId;
                capability.service = request.serviceIdentity;

                try {
                    channel.writeProtobuf(capability);
                } catch (IOException e) {
                    return;
                }
            }
        }).start();
    }

}
