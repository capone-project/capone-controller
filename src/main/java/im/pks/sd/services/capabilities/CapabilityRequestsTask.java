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

import android.os.AsyncTask;
import im.pks.sd.entities.CapabilityRequestTo;
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.ConnectTask;
import im.pks.sd.protocol.RequestTask;
import im.pks.sd.protocol.SessionTask;
import nano.Capabilities;
import org.abstractj.kalium.keys.VerifyKey;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CapabilityRequestsTask extends AsyncTask<Void, Void, CapabilityRequestsTask.Result> implements ConnectTask.Handler {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ServiceDescriptionTo service;
    private final List<ParameterTo> parameters;

    public static class Result {
        public final Throwable t;

        public Result(Throwable t) {
            this.t = t;
        }

    }

    public interface RequestListener {
        void onRequestReceived(CapabilityRequestTo request, Runnable accept);
    }

    private RequestListener listener;
    private SessionTask sessionTask;

    public CapabilityRequestsTask(ServiceDescriptionTo service, List<ParameterTo> parameters) {
        this.service = service;
        this.parameters = parameters;
    }

    public void setRequestListener(RequestListener listener) {
        this.listener = listener;
    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            connect();
            return null;
        } catch (IOException | VerifyKey.SignatureException e) {
            return new Result(e);
        }
    }

    public void connect() throws IOException, VerifyKey.SignatureException {
        sessionTask = new SessionTask(service, parameters, this);
        sessionTask.startSession();
        sessionTask = null;
    }

    @Override
    public void handleConnection(final Channel channel)
            throws IOException, VerifyKey.SignatureException {
        final Capabilities.CapabilityRequest request = new Capabilities.CapabilityRequest();

        while (channel.readProtobuf(request) != null) {
            final CapabilityRequestTo requestTo;
            try {
                requestTo = new CapabilityRequestTo(request, new Date());
            } catch (RuntimeException e) {
                continue;
            }

            listener.onRequestReceived(requestTo, new Runnable() {
                @Override
                public void run() {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            accept(channel, requestTo);
                        }
                    });
                }
            });
        }
    }

    private void accept(Channel channel, CapabilityRequestTo request) {
        ArrayList<ParameterTo> serviceParameters = new ArrayList<>();
        for (ParameterTo parameter : request.parameters) {
            if (!parameter.name.equals("service-parameters")) {
                continue;
            }

            String[] keyValue = StringUtils.split(parameter.value, "=", 2);
            if (keyValue == null)
                continue;
            else if (keyValue.length == 1)
                serviceParameters.add(new ParameterTo(keyValue[0], null));
            else
                serviceParameters.add(new ParameterTo(keyValue[0], keyValue[1]));
        }

        RequestTask requestTask = new RequestTask(request.invokerIdentity,
                                                  request.serviceIdentity,
                                                  request.serviceAddress,
                                                  Integer.valueOf(request.servicePort),
                                                  serviceParameters);
        RequestTask.Session session;
        try {
            session = requestTask.requestSession();
        } catch (IOException | VerifyKey.SignatureException e) {
            return;
        }

        Capabilities.Capability capability = new Capabilities.Capability();
        capability.requestid = request.requestId;
        capability.sessionid = session.sessionId;
        capability.identity = request.invokerIdentity.toBytes();
        capability.service = request.serviceIdentity.toBytes();

        try {
            channel.writeProtobuf(capability);
        } catch (IOException e) {
            return;
        }
    }

    public void cancel() {
        if (sessionTask != null) {
            sessionTask.cancel();
        }
        executor.shutdown();
    }

}
