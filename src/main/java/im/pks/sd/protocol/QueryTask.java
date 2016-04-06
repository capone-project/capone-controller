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
import im.pks.sd.controller.invoke.QueryResults;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class QueryTask extends AsyncTask<QueryTask.Parameters, QueryResults, Void> {

    public static class Parameters {
        public final SigningKey localKey;
        public final ServerTo server;
        public final ServiceTo service;

        public Parameters(SigningKey localKey, ServerTo server, ServiceTo service) {
            this.localKey = localKey;
            this.server = server;
            this.service = service;
        }
    }

    private Channel channel = null;

    @Override
    protected Void doInBackground(Parameters... params) {
        for (Parameters param : params) {
            try {
                if (isCancelled())
                    return null;

                channel = new TcpChannel(param.server.address, param.service.port);
                channel.connect();

                Connect.ConnectionInitiationMessage initiation = new Connect
                        .ConnectionInitiationMessage();
                initiation.type = Connect.ConnectionInitiationMessage.QUERY;
                channel.writeProtobuf(initiation);

                VerifyKey remoteKey = new VerifyKey(param.server.publicKey, Encoder.HEX);
                channel.enableEncryption(param.localKey, remoteKey);

                Connect.QueryResults queryResults = new Connect.QueryResults();
                channel.readProtobuf(queryResults);

                publishProgress(convertQuery(param, queryResults));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (VerifyKey.SignatureException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (channel != null)
                        channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private QueryResults convertQuery(Parameters params, Connect.QueryResults queryResults) {
        List<QueryResults.Parameter> parameters = new ArrayList<>();
        for (Connect.Parameter parameter : queryResults.parameters) {
            parameters.add(new QueryResults.Parameter(parameter.key,
                                                      Arrays.asList(parameter.values)));
        }

        return new QueryResults(params.server, params.service, queryResults.type,
                                queryResults.location, queryResults.version, parameters);
    }

    public void cancel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public abstract void onProgressUpdate(QueryResults... details);

}
