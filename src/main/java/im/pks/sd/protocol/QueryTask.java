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
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.entities.ServiceTo;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;

public abstract class QueryTask
        extends AsyncTask<QueryTask.Parameters, ServiceDescriptionTo, Throwable> {

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
    protected Throwable doInBackground(Parameters... params) {
        for (Parameters param : params) {
            try {
                if (isCancelled())
                    return null;
                VerifyKey remoteKey = new VerifyKey(param.server.publicKey, Encoder.HEX);

                channel = new TcpChannel(param.server.address, param.service.port);
                channel.connect();
                channel.enableEncryption(param.localKey, remoteKey);

                Connect.ConnectionInitiationMessage initiation = new Connect
                                                                             .ConnectionInitiationMessage();
                initiation.type = Connect.ConnectionInitiationMessage.QUERY;
                channel.writeProtobuf(initiation);

                Connect.ServiceDescription queryResults = new Connect.ServiceDescription();
                channel.readProtobuf(queryResults);

                publishProgress(convertQuery(param, queryResults));

                return null;
            } catch (VerifyKey.SignatureException | IOException e) {
                return e;
            } finally {
                try {
                    if (channel != null)
                        channel.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }

        return null;
    }

    private ServiceDescriptionTo convertQuery(Parameters params, Connect.ServiceDescription queryResults) {
        ArrayList<ParameterTo> parameters = new ArrayList<>();
        for (Connect.Parameter parameter : queryResults.parameters) {
            parameters.add(new ParameterTo(parameter.key, parameter.value));
        }

        return new ServiceDescriptionTo(params.server, params.service, queryResults.type,
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
    public abstract void onProgressUpdate(ServiceDescriptionTo... description);

}
