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

package im.pks.sd.services.invoke;

import android.os.AsyncTask;
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.protocol.RequestTask;
import im.pks.sd.protocol.SessionTask;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvokePluginTask extends AsyncTask<Void, Void, Throwable> {

    private final List<ParameterTo> parameters;
    private final ServiceDescriptionTo invoker;
    private final ServiceDescriptionTo service;

    public InvokePluginTask(ServiceDescriptionTo invoker,
                            ServiceDescriptionTo service,
                            List<ParameterTo> parameters) {
        this.invoker = invoker;
        this.service = service;
        this.parameters = parameters;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            RequestTask.Session session = sendSessionRequest();
            sendInvokeRequest(session);
            return null;
        } catch (IOException | VerifyKey.SignatureException e) {
            return e;
        }
    }

    private RequestTask.Session sendSessionRequest()
            throws IOException, VerifyKey.SignatureException {
        /* TODO: fill parameters with parameters for the specific invoker */
        List<ParameterTo> parameters = Collections.emptyList();
        VerifyKey identity = new VerifyKey(invoker.server.publicKey, Encoder.HEX);

        RequestTask request = new RequestTask(identity, service, parameters);

        return request.requestSession();
    }

    private void sendInvokeRequest(RequestTask.Session session) throws IOException, VerifyKey.SignatureException {
        List<ParameterTo> parameters = new ArrayList<>();
        parameters.addAll(this.parameters);
        parameters.add(new ParameterTo("sessionid", Integer.toString(session.sessionId)));

        SessionTask sessionTask = new SessionTask(invoker, parameters, null);
        sessionTask.startSession();
    }

}
