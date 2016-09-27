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

package com.github.capone.entities;

import nano.Core;
import org.bouncycastle.jcajce.provider.digest.Blake2b;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CapabilityTo {

    public static final int RIGHT_EXEC = 1 << 0;
    public static final int RIGHT_TERMINATE = 1 << 1;

    public static final int SECRET_LENGTH = 32;

    public class ChainSegment {
        public final int rights;
        public final SignatureKeyTo entity;

        public ChainSegment(int rights, SignatureKeyTo entity) {
            this.rights = rights;
            this.entity = entity;
        }
    }

    public final byte[] secret;
    public final List<ChainSegment> chain;

    protected CapabilityTo(byte[] secret) {
        this(secret, new ArrayList<ChainSegment>());
    }

    protected CapabilityTo(byte[] secret, final List<ChainSegment> chain) {
        if (secret.length != SECRET_LENGTH)
            throw new RuntimeException("Invalid capability secret length");
        this.secret = secret;
        this.chain = chain;
    }

    public CapabilityTo(Core.CapabilityMessage msg) {
        if (msg.secret.length != SECRET_LENGTH)
            throw new RuntimeException("Invalid capability secret length");
        secret = msg.secret;

        if (msg.chain != null) {
            chain = new ArrayList<>(msg.chain.length);
            for (Core.CapabilityMessage.Chain segment : msg.chain) {
                chain.add(new ChainSegment(segment.rights, new SignatureKeyTo(segment.entity)));
            }
        } else {
            chain = new ArrayList<>();
        }
    }

    public Core.CapabilityMessage toMessage() {
        Core.CapabilityMessage msg = new Core.CapabilityMessage();

        msg.secret = secret;

        if (chain.size() > 0) {
            msg.chain = new Core.CapabilityMessage.Chain[chain.size()];
            for (int i = 0; i < chain.size(); i++) {
                msg.chain[i] = new Core.CapabilityMessage.Chain();
                msg.chain[i].rights = chain.get(i).rights;
                msg.chain[i].entity = chain.get(i).entity.toMessage();
            }
        } else {
            msg.chain = null;
        }

        return msg;
    }

    public CapabilityTo createReference(int rights, final SignatureKeyTo entity) {
        Blake2b.Blake2b256 blake = new Blake2b.Blake2b256();
        blake.update(entity.key.toBytes());
        blake.update(ByteBuffer.allocate(4).putInt(rights).order(ByteOrder.nativeOrder()).array());
        blake.update(secret);
        byte[] secret = blake.digest();

        ArrayList<ChainSegment> segments = new ArrayList<>(chain.size() + 1);
        segments.addAll(chain);
        segments.add(new ChainSegment(rights, entity));

        return new CapabilityTo(secret, segments);
    }

}
