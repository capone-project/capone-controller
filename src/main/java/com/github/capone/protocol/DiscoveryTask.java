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
import com.github.capone.protocol.entities.ServerTo;
import nano.Discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class DiscoveryTask extends AsyncTask<Void, ServerTo, Throwable> {

    public static final int DISCOVERY_PORT = 6667;
    public static final String BROADCAST_ADDRESS = "224.0.0.1";

    private DatagramSocket socket;

    @Override
    protected Throwable doInBackground(Void... ignored) {
        Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
        discoverMessage.version = Client.PROTOCOL_VERSION;

        try {
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            UdpChannel channel = UdpChannel.createFromSocket(socket,
                                                                      broadcastAddress,
                                                                      DISCOVERY_PORT);

            while (true) {
                channel.writeProtobuf(discoverMessage);

                while (true) {
                    if (this.isCancelled()) {
                        return null;
                    }

                    try {
                        DatagramPacket announcePacket = channel.peek(512);
                        Discovery.DiscoverResult announceMessage = new Discovery.DiscoverResult();
                        channel.readProtobuf(announceMessage);

                        ServerTo server = ServerTo.fromAnnounce(
                                announcePacket.getAddress().getCanonicalHostName(),
                                announceMessage);

                        publishProgress(server);
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            return e;
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    public void cancel() {
        if (socket != null)
            socket.close();
        super.cancel(true);
    }

}
