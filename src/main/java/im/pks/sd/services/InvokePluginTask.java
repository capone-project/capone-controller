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

package im.pks.sd.services;

import android.os.AsyncTask;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.protocol.RequestTask;
import im.pks.sd.protocol.SessionTask;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvokePluginTask extends AsyncTask<Void, Void, Void> {

    private final List<ServiceDescriptionTo.Parameter> parameters;
    private final ServiceDescriptionTo invoker;
    private final ServiceDescriptionTo service;

    public InvokePluginTask(ServiceDescriptionTo invoker,
                            ServiceDescriptionTo service,
                            List<ServiceDescriptionTo.Parameter> parameters) {
        this.invoker = invoker;
        this.service = service;
        this.parameters = parameters;
    }

    @Override
    protected Void doInBackground(Void... params) {
        sendServiceRequest();
        return null;
    }

    private void sendServiceRequest() {
        /* TODO: fill parameters with parameters for the specific invoker */
        List<ServiceDescriptionTo.Parameter> parameters = Collections.emptyList();
        VerifyKey identity = new VerifyKey(invoker.server.publicKey, Encoder.HEX);

        RequestTask request = new RequestTask(identity, service, parameters) {
            @Override
            public void onPostExecute(Session session) {
                sendInvokeRequest(session);
            }
        };

        request.execute();
    }

    private void sendInvokeRequest(RequestTask.Session session) {
        List<ServiceDescriptionTo.Parameter> parameters = new ArrayList<>();
        parameters.addAll(this.parameters);
        parameters.add(new ServiceDescriptionTo.Parameter("sessionid",
                                                          Integer.toString(session.sessionId)));

        SessionTask sessionTask = new SessionTask(invoker, parameters, null);
        sessionTask.execute();
    }

}
