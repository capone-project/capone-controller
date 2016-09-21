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
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import com.github.capone.persistence.Identity;
import com.github.capone.protocol.Client;
import com.google.protobuf.nano.MessageNano;
import nano.Invoke;

public class InvokePluginTask extends AsyncTask<Void, Void, Throwable> {

    private final ServiceDescriptionTo invoker;
    private final ServiceDescriptionTo service;
    private final MessageNano serviceParameters;

    public InvokePluginTask(ServiceDescriptionTo invoker,
                            ServiceDescriptionTo service,
                            MessageNano serviceParameters) {
        this.invoker = invoker;
        this.service = service;
        this.serviceParameters = serviceParameters;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        Client serviceClient = new Client(Identity.getSigningKey(), service.server);
        SessionTo serviceSession = serviceClient.request(invoker.service, serviceParameters);

        CapabilityTo reference = serviceSession.capability.createReference(
                CapabilityTo.RIGHT_EXEC | CapabilityTo.RIGHT_TERMINATE,
                invoker.server.signatureKey);

        Invoke.InvokeParams parameters = new Invoke.InvokeParams();
        parameters.sessionid = serviceSession.identifier;
        parameters.cap = reference.toMessage();
        parameters.serviceIdentity = service.server.signatureKey.toMessage();
        parameters.serviceAddress = service.server.address;
        parameters.servicePort = service.service.port;
        parameters.serviceType = service.type;

        Client invokerClient = new Client(Identity.getSigningKey(), invoker.server);
        SessionTo invokerSession = invokerClient.request(invoker.service, parameters);
        invokerClient.connect(invoker.service, invokerSession, null);

        return null;
    }
}
