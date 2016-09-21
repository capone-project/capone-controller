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
import com.github.capone.entities.ServerTo;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import com.github.capone.persistence.Identity;
import com.google.protobuf.nano.MessageNano;

public class RequestTask extends AsyncTask<Void, Void, RequestTask.Result> {

    public static class Result {
        public final Throwable throwable;
        public final SessionTo session;

        public Result(Throwable throwable) {
            this.session = null;
            this.throwable = throwable;
        }

        public Result(SessionTo session) {
            this.session = session;
            this.throwable = null;
        }
    }

    private final Client client;
    private final ServiceDescriptionTo service;
    private final byte[] parameters;

    public RequestTask(ServerTo server, ServiceDescriptionTo service, MessageNano parameters) {
        this.client = new Client(Identity.getSigningKey(), server);
        this.service = service;
        this.parameters = MessageNano.toByteArray(parameters);
    }

    public RequestTask(ServerTo server, ServiceDescriptionTo service, byte[] parameters) {
        this.client = new Client(Identity.getSigningKey(), server);
        this.service = service;
        this.parameters = parameters;
    }

    @Override
    protected Result doInBackground(Void... params) {
        return new Result(client.request(service, parameters));
    }

    public void cancel() {
        client.disconnect();
    }

}
