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

package im.pks.sd.controller.query;

import android.os.AsyncTask;
import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.TcpChannel;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class QueryTask extends AsyncTask<QueryTask.QueryParameters, ServiceDetails, Void> {

    public static class QueryParameters {
        public final SigningKey localKey;
        public final Server server;
        public final Service service;

        public QueryParameters(SigningKey localKey, Server server, Service service) {
            this.localKey = localKey;
            this.server = server;
            this.service = service;
        }
    }

    @Override
    protected Void doInBackground(QueryParameters... params) {
        for (QueryParameters param : params) {
            Channel channel = null;

            try {
                channel = TcpChannel.createFromHost(param.server.address, param.service.port);

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

    private ServiceDetails convertQuery(QueryParameters params, Connect.QueryResults queryResults) {
        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        for (Connect.Parameter parameter : queryResults.parameters) {
            parameters.add(new ServiceDetails.Parameter(parameter.key,
                    Arrays.asList(parameter.values)));
        }

        return new ServiceDetails(params.server, params.service, queryResults.subtype,
                queryResults.location, queryResults.version, parameters);
    }

    @Override
    public abstract void onProgressUpdate(ServiceDetails... details);

}