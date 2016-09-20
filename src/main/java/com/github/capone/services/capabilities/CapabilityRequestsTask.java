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

package com.github.capone.services.capabilities;

import android.os.AsyncTask;
import com.github.capone.entities.*;
import com.github.capone.protocol.*;
import com.google.protobuf.nano.MessageNano;
import nano.Capabilities;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CapabilityRequestsTask extends AsyncTask<Void, Void, CapabilityRequestsTask.Result>
        implements Client.SessionHandler {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ServiceDescriptionTo service;
    private final MessageNano parameters;

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

    public CapabilityRequestsTask(ServiceDescriptionTo service, MessageNano parameters) {
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
    public void onSessionStarted(ServiceTo service, SessionTo session, final Channel channel) {
        final Capabilities.CapabilitiesRequest request = new Capabilities.CapabilitiesRequest();

        try {
            while (request.clear() != null && channel.readProtobuf(request) != null) {
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
        } catch (IOException e) {
            /* ignore */
        }
    }

    @Override
    public void onError() {

    }

    private void accept(Channel channel, CapabilityRequestTo request) {
        RequestTask requestTask = new RequestTask(request.serviceIdentity.key,
                                                  request.serviceAddress,
                                                  Integer.valueOf(request.servicePort),
                                                  request.parameters);
        SessionTo session;
        try {
            session = requestTask.requestSession();
        } catch (IOException | VerifyKey.SignatureException e) {
            return;
        }

        Capabilities.Capability capability = new Capabilities.Capability();
        capability.requestid = request.requestId;
        capability.capability = session.capability.createReference(CapabilityTo.RIGHT_EXEC,
                                                                   request.requesterIdentity).toMessage();
        capability.serviceIdentity = request.serviceIdentity.toMessage();

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
