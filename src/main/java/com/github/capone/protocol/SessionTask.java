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

package com.github.capone.protocol;

import android.os.AsyncTask;
import com.github.capone.persistence.SigningKeyRecord;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.protocol.entities.Session;
import com.google.protobuf.nano.MessageNano;

import java.io.IOException;

public class SessionTask extends AsyncTask<Void, Void, Throwable> {

    private final Server server;
    private final ServiceDescription service;
    private final MessageNano parameters;
    private final Client.SessionHandler handler;

    private Client client;

    public SessionTask(Server server, ServiceDescription service,
                       MessageNano parameters, Client.SessionHandler handler) {
        this.server = server;
        this.service = service;
        this.parameters = parameters;
        this.handler = handler;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            SigningKey key = SigningKeyRecord.getSigningKey();
            client = new Client(key, server);
            Session session = client.request(service, parameters);
            client.connect(service, session, handler);
            return null;
        } catch (IOException | ProtocolException e) {
            return e;
        }
    }

    public void cancel() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }

}
