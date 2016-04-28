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
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class ConnectTask extends AsyncTask<Void, Void, Void> {

    public interface Handler {
        void handleConnection(Channel channel);
    }

    private final int sessionId;
    private final ServiceDescriptionTo service;

    private Channel channel;
    private Handler handler;

    public ConnectTask(int sessionId, ServiceDescriptionTo service) {
        this.sessionId = sessionId;
        this.service = service;
    }

    @Override
    protected Void doInBackground(Void... params) {
        connect();
        return null;
    }

    public void connect() {
        Connect.ConnectionInitiationMessage connectionInitiation = new Connect.ConnectionInitiationMessage();
        connectionInitiation.type = Connect.ConnectionInitiationMessage.CONNECT;
        Connect.SessionInitiationMessage sessionInitiation = new Connect.SessionInitiationMessage();
        sessionInitiation.sessionid = sessionId;

        try {
            channel = new TcpChannel(service.server.address, service.service.port);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(),
                                     new VerifyKey(service.server.publicKey, Encoder.HEX));
            channel.writeProtobuf(connectionInitiation);
            channel.writeProtobuf(sessionInitiation);

            if (handler != null) {
                handler.handleConnection(channel);
            }

            channel.close();
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
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void cancel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
            }
        }
    }

}
