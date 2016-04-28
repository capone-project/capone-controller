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

package im.pks.sd.protocol;

import android.os.AsyncTask;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.persistence.Identity;
import org.abstractj.kalium.keys.VerifyKey;

import java.util.List;

public class SessionTask extends AsyncTask<Void, Void, Void> {

    private final ServiceDescriptionTo service;
    private final List<ServiceDescriptionTo.Parameter> parameters;
    private final ConnectTask.Handler handler;

    private RequestTask request;
    private ConnectTask connect;

    public SessionTask(ServiceDescriptionTo service, List<ServiceDescriptionTo.Parameter> parameters,
                       ConnectTask.Handler handler) {
        this.service = service;
        this.parameters = parameters;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        VerifyKey identity = Identity.getSigningKey().getVerifyKey();

        request = new RequestTask(identity, service, parameters);
        RequestTask.Session session = request.requestSession();
        request = null;

        connect = new ConnectTask(session.sessionId, service);
        connect.setHandler(handler);
        connect.connect();

        return null;
    }

    public void cancel() {
        if (request != null) {
            request.cancel();
        }
        if (connect != null) {
            connect.cancel();
        }
    }

}
