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
import com.github.capone.persistence.Identity;
import com.google.protobuf.nano.MessageNano;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class SessionTask extends AsyncTask<Void, Void, Throwable> {

    private final ServiceDescriptionTo service;
    private final MessageNano parameters;
    private final Client.SessionHandler handler;

    private Client client;

    public SessionTask(ServiceDescriptionTo service, MessageNano parameters,
                       Client.SessionHandler handler) {
        this.service = service;
        this.parameters = parameters;
        this.handler = handler;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            startSession();
            return null;
        } catch (IOException | VerifyKey.SignatureException e) {
            return e;
        }
    }

    public void startSession() throws IOException, VerifyKey.SignatureException {
        client = new Client(Identity.getSigningKey(), service.server);
        SessionTo session = client.request(service.service, parameters);
        client.connect(service.service, session, handler);
    }

    public void cancel() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }

}
