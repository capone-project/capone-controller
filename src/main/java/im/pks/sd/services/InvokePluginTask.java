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
import im.pks.sd.controller.invoke.QueryResults;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.ConnectTask;
import im.pks.sd.protocol.RequestTask;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvokePluginTask extends AsyncTask<Void, Void, Void> {

    private final List<QueryResults.Parameter> parameters;
    private final QueryResults invoker;
    private final QueryResults service;

    public InvokePluginTask(QueryResults invoker,
                            QueryResults service,
                            List<QueryResults.Parameter> parameters) {
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
        RequestTask request = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                sendInvokeRequest(session);
            }
        };

        /* TODO: fill parameters with parameters for the specific invoker */
        RequestTask.Parameters parameters =
                new RequestTask.Parameters(new VerifyKey(invoker.server.publicKey, Encoder.HEX),
                                           service,
                                           Collections.<QueryResults.Parameter>emptyList());
        request.execute(parameters);
    }

    private void sendInvokeRequest(RequestTask.Session session) {
        List<QueryResults.Parameter> parameters = new ArrayList<>();
        parameters.addAll(this.parameters);
        parameters.add(new QueryResults.Parameter("sessionid",
                                                  Integer.toString(session.sessionId)));

        RequestTask invocationServiceRequest = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                startSession(session);
            }
        };

        RequestTask.Parameters request =
                new RequestTask.Parameters(Identity.getSigningKey().getVerifyKey(),
                                           invoker,
                                           parameters);
        invocationServiceRequest.execute(request);
    }

    private void startSession(RequestTask.Session session) {
        ConnectTask connectTask = new ConnectTask() {
            @Override
            public void handleConnection(Channel channel) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        };

        ConnectTask.Parameters connectParameter =
                new ConnectTask.Parameters(session.sessionId,
                                           invoker.server,
                                           invoker.service);
        connectTask.execute(connectParameter);
    }

}
