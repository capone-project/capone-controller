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

package com.github.capone.protocol.entities;

import com.github.capone.protocol.crypto.VerifyKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

public class CapabilityTest {

    @Test
    public void creatingReferenceSucceeds() throws VerifyKey.InvalidKeyException {
        Capability root = new Capability(new byte[Capability.SECRET_LENGTH]);
        Identity key = new Identity(VerifyKey.fromBytes(new byte[VerifyKey.BYTES]));
        Capability ref = root.createReference(
                Capability.RIGHT_EXEC | Capability.RIGHT_TERMINATE, key);

        Assert.assertEquals(Hex.toHexString(ref.secret),
                            "ef65d681590e6c95d923bb0da71851cc4902491f054ec767b8c3aa697df1c913");
    }

}
