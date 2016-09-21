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

public class ConnectTask extends AsyncTask<Void, Void, Void> {

    private final SessionTo session;
    private final ServiceDescriptionTo service;

    private Client client;
    private Client.SessionHandler handler;

    public ConnectTask(ServerTo server, ServiceDescriptionTo service, SessionTo session) {
        this.session = session;
        this.service = service;
        this.client = new Client(Identity.getSigningKey(), server);
    }

    @Override
    protected Void doInBackground(Void... params) {
        client.connect(service, session, handler);
        return null;
    }

    public void setHandler(Client.SessionHandler handler) {
        this.handler = handler;
    }

    public void cancel() {
        client.disconnect();
    }

}
