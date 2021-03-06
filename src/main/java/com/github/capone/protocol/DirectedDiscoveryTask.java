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

package com.github.capone.protocol;

import android.os.AsyncTask;
import com.github.capone.persistence.ServerRecord;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.crypto.SymmetricKey;
import com.github.capone.protocol.crypto.VerifyKey;
import com.github.capone.protocol.entities.Server;
import nano.Discovery;

import java.io.IOException;

public class DirectedDiscoveryTask extends AsyncTask<Void, Void, DirectedDiscoveryTask.Result> {

    public static class Result {
        public final Throwable throwable;
        public final Server server;

        public Result(Server server) {
            this.server = server;
            this.throwable = null;
        }

        public Result(Throwable throwable) {
            this.server = null;
            this.throwable = throwable;
        }
    }

    private final SigningKey key;
    private final ServerRecord server;

    private TcpChannel channel;

    public DirectedDiscoveryTask(final SigningKey key, final ServerRecord server) {
        this.key = key;
        this.server = server;
    }

    @Override
    protected Result doInBackground(Void... params) {
        channel = new TcpChannel(server.getAddress(), DiscoveryTask.DISCOVERY_PORT);

        try {
            channel.connect();
            channel.enableEncryption(key, server.getPublicKey());

            Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
            discoverMessage.version = Client.PROTOCOL_VERSION;
            channel.writeProtobuf(discoverMessage);

            Discovery.DiscoverResult announceMessage = new Discovery.DiscoverResult();
            channel.readProtobuf(announceMessage);

            return new Result(Server.fromAnnounce(server.getAddress(), announceMessage));
        } catch (VerifyKey.SignatureException |
                         VerifyKey.InvalidKeyException |
                         IOException |
                         SymmetricKey.InvalidKeyException |
                         SymmetricKey.EncryptionException |
                         SymmetricKey.DecryptionException e) {
            return new Result(e);
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                // ignore
            } finally {
                channel = null;
            }
        }

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
