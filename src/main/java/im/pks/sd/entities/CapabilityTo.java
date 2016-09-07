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

package im.pks.sd.entities;

import nano.Connect;
import org.abstractj.kalium.Sodium;
import org.abstractj.kalium.SodiumConstants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CapabilityTo {

    public static final int RIGHT_EXEC = 1;
    public static final int RIGHT_TERMINATE = 2;

    public static final int SECRET_LENGTH = 32;

    public class ChainSegment {
        public int rights;
        public byte[] entity;

        public ChainSegment(int rights, byte[] entity) {
            this.rights = rights;
            this.entity = entity;
        }
    }

    public final byte[] secret;
    public final List<ChainSegment> chain;

    private CapabilityTo(byte[] secret, final List<ChainSegment> chain) {
        this.secret = secret;
        this.chain = chain;
    }

    public CapabilityTo(Connect.CapabilityMessage msg) {
        secret = msg.secret;

        if (msg.chain != null) {
            chain = new ArrayList<>(msg.chain.length);
            for (Connect.CapabilityMessage.Chain segment : msg.chain) {
                chain.add(new ChainSegment(segment.rights, segment.entity));
            }
        } else {
            chain = new ArrayList<>();
        }
    }

    public Connect.CapabilityMessage toMessage() {
        Connect.CapabilityMessage msg = new Connect.CapabilityMessage();

        msg.secret = secret;

        if (chain.size() > 0) {
            msg.chain = new Connect.CapabilityMessage.Chain[chain.size()];
            for (int i = 0; i < chain.size(); i++) {
                msg.chain[i] = new Connect.CapabilityMessage.Chain();
                msg.chain[i].rights = chain.get(i).rights;
                msg.chain[i].entity = chain.get(i).entity;
            }
        } else {
            msg.chain = null;
        }

        return msg;
    }

    public CapabilityTo createReference(int rights, final byte[] identity) {
        ByteBuffer buffer = ByteBuffer.allocate(SodiumConstants.PUBLICKEY_BYTES + 4 + SECRET_LENGTH);
        buffer.put(identity);
        buffer.putInt(rights);
        buffer.put(secret);

        byte[] secret = new byte[SECRET_LENGTH];
        Sodium.crypto_generichash_blake2b(secret, secret.length, buffer.array(),
                                          buffer.array().length, new byte[0], 0);

        ArrayList<ChainSegment> segments = new ArrayList<>(chain.size() + 1);
        segments.addAll(chain);
        segments.add(new ChainSegment(rights, identity));

        return new CapabilityTo(secret, segments);
    }

}
