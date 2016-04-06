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
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import nano.Connect;
import org.abstractj.kalium.crypto.SecretBox;

import java.io.IOException;

public abstract class ConnectTask extends AsyncTask<ConnectTask.Parameters, Void, Void> {

    public static class Parameters {
        public final SecretBox key;
        public final int sessionId;
        public final ServerTo server;
        public final ServiceTo service;

        public Parameters(int sessionId, SecretBox key, ServerTo server, ServiceTo service) {
            this.sessionId = sessionId;
            this.key = key;
            this.server = server;
            this.service = service;
        }
    }

    private Channel channel = null;

    @Override
    protected Void doInBackground(Parameters... params) {
        Parameters param = params[0];

        Connect.ConnectionInitiationMessage connectionInitiation = new Connect.ConnectionInitiationMessage();
        connectionInitiation.type = Connect.ConnectionInitiationMessage.CONNECT;
        Connect.SessionInitiationMessage sessionInitiation = new Connect.SessionInitiationMessage();
        sessionInitiation.sessionid = param.sessionId;

        try {
            channel = new TcpChannel(param.server.address, param.service.port);
            channel.connect();
            channel.writeProtobuf(connectionInitiation);
            channel.writeProtobuf(sessionInitiation);
            channel.enableEncryption(param.key);

            handleConnection(channel);
        } catch (IOException e) {
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

    public abstract void handleConnection(Channel channel);

}
