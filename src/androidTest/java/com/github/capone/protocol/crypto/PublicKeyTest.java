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

package com.github.capone.protocol.crypto;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import junit.framework.Assert;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PublicKeyTest {

    private PublicKey key;

    @Test
    public void keyFromBytesSucceeds() throws PublicKey.InvalidKeyException {
        key = PublicKey.fromBytes(new byte[PublicKey.BYTES]);
        Assert.assertEquals(Hex.toHexString(new byte[PublicKey.BYTES]),
                Hex.toHexString(key.toBytes()));
    }

    @Test(expected = PublicKey.InvalidKeyException.class)
    public void keyFromInvalidByteLengthFails() throws PublicKey.InvalidKeyException {
        PublicKey.fromBytes(new byte[PublicKey.BYTES + 1]);
    }

}
