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
import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public abstract class ConnectTask extends AsyncTask<ConnectTask.ConnectParameters, Void, Void> {

    public static class ConnectParameters {
        public final SigningKey localKey;
        public final Server server;
        public final Service service;

        public ConnectParameters(SigningKey localKey, Server server, Service service) {
            this.localKey = localKey;
            this.server = server;
            this.service = service;
        }
    }

    private Channel channel = null;

    @Override
    protected Void doInBackground(ConnectParameters... params) {
        ConnectParameters param = params[0];
        VerifyKey remoteKey = new VerifyKey(param.server.publicKey, Encoder.HEX);

        Connect.ConnectionInitiationMessage initiation = new Connect.ConnectionInitiationMessage();
        initiation.type = Connect.ConnectionInitiationMessage.CONNECT;

        try {
            channel = new TcpChannel(param.server.address, param.service.port);
            channel.connect();
            channel.writeProtobuf(initiation);
            channel.enableEncryption(param.localKey, remoteKey);

            handleConnection(channel);
        } catch (VerifyKey.SignatureException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null)
                    channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
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
    public abstract void onPostExecute(Void result);

    public abstract void handleConnection(Channel channel);

}
