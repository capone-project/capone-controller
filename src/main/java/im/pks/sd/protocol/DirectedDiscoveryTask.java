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
import im.pks.sd.persistence.Server;
import nano.Discovery;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class DirectedDiscoveryTask extends AsyncTask<Void, Void, ServerTo> {

    private final SigningKey key;
    private final Server server;

    private TcpChannel channel;

    public DirectedDiscoveryTask(final SigningKey key, final Server server) {
        this.key = key;
        this.server = server;
    }

    @Override
    protected ServerTo doInBackground(Void... params) {
        channel = new TcpChannel(server.getAddress(), DiscoveryTask.REMOTE_DISCOVERY_PORT);

        try {
            channel.connect();
            channel.enableEncryption(key, new VerifyKey(server.getPublicKey(), Encoder.HEX));

            Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
            discoverMessage.version = "0.0.1";
            discoverMessage.port = 0;
            channel.writeProtobuf(discoverMessage);

            Discovery.AnnounceMessage announceMessage = new Discovery.AnnounceMessage();
            channel.readProtobuf(announceMessage);

            return ServerTo.fromAnnounce(server.getAddress(), announceMessage);
        } catch (VerifyKey.SignatureException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                // do nothing
            } finally {
                channel = null;
            }
        }

        return null;
    }

    public void cancel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                channel = null;
            }
        }
        super.cancel(true);
    }

}
