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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TcpChannel extends Channel {
    private Socket socket;
    private String host;
    int port;

    public TcpChannel(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = new Socket();
    }

    @Override
    protected void write(byte[] msg, int len) throws IOException {
        socket.getOutputStream().write(msg, 0, len);
    }

    @Override
    protected int read(byte[] msg, int len) throws IOException {
        return socket.getInputStream().read(msg, 0, len);
    }

    @Override
    public void connect() throws IOException {
        SocketAddress address = new InetSocketAddress(host, port);
        socket.connect(address);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
