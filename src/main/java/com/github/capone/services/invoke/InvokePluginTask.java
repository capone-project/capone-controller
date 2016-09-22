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

package com.github.capone.services.invoke;

import android.os.AsyncTask;
import com.github.capone.entities.CapabilityTo;
import com.github.capone.entities.ServerTo;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import com.github.capone.persistence.Identity;
import com.github.capone.protocol.Client;
import com.google.protobuf.nano.MessageNano;
import nano.Invoke;

public class InvokePluginTask extends AsyncTask<Void, Void, Throwable> {

    private final ServerTo invokerServer;
    private final ServiceDescriptionTo invoker;
    private final ServerTo serviceServer;
    private final ServiceDescriptionTo service;
    private final MessageNano serviceParameters;

    public InvokePluginTask(ServerTo invokerServer, ServiceDescriptionTo invoker,
                            ServerTo serviceServer, ServiceDescriptionTo service,
                            MessageNano serviceParameters) {
        this.invokerServer = invokerServer;
        this.invoker = invoker;
        this.serviceServer = serviceServer;
        this.service = service;
        this.serviceParameters = serviceParameters;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        Client serviceClient = new Client(Identity.getSigningKey(), serviceServer);
        SessionTo serviceSession;

        try {
            serviceSession = serviceClient.request(service, serviceParameters);
        } catch (Exception e) {
            return e;
        }

        CapabilityTo reference = serviceSession.capability.createReference(
                CapabilityTo.RIGHT_EXEC | CapabilityTo.RIGHT_TERMINATE,
                invokerServer.signatureKey);

        Invoke.InvokeParams parameters = new Invoke.InvokeParams();
        parameters.sessionid = serviceSession.identifier;
        parameters.cap = reference.toMessage();
        parameters.serviceIdentity = serviceServer.signatureKey.toMessage();
        parameters.serviceAddress = serviceServer.address;
        parameters.servicePort = service.port;
        parameters.serviceType = service.type;

        Client invokerClient = new Client(Identity.getSigningKey(), invokerServer);
        try {
            SessionTo invokerSession = invokerClient.request(invoker, parameters);
            invokerClient.connect(invoker, invokerSession, null);
        } catch (Exception e) {
            return e;
        }

        return null;
    }
}
