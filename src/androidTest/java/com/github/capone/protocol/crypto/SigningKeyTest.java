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
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SigningKeyTest {

    private SigningKey key;

    @Before
    public void setUp() throws SigningKey.InvalidSeedException {
        key = SigningKey.fromSeed(StringUtils.repeat("0", SigningKey.BYTES * 2));
    }

    @Test
    public void signingKeyFromValidSeedSucceeds() throws SigningKey.InvalidSeedException {
        key = SigningKey.fromSeed(StringUtils.repeat("a", SigningKey.BYTES * 2));
        Assert.assertEquals(StringUtils.repeat("a", SigningKey.BYTES * 2), key.toString());
    }

    @Test(expected = SigningKey.InvalidSeedException.class)
    public void signingKeyFromSeedWithInvalidCharsThrows() throws SigningKey.InvalidSeedException {
        SigningKey.fromSeed(StringUtils.repeat("x", SigningKey.BYTES * 2));
    }

    @Test(expected = SigningKey.InvalidSeedException.class)
    public void signingKeyFromSeedWithInvalidLengthThrows() throws SigningKey.InvalidSeedException {
        SigningKey.fromSeed(StringUtils.repeat("a", SigningKey.BYTES * 2 + 1));
    }

    @Test
    public void signatureIsVerifiable() throws VerifyKey.SignatureException {
        byte[] signature = key.sign("bla".getBytes());
        key.getVerifyKey().verify("bla".getBytes(), signature);
    }

    @Test(expected = VerifyKey.SignatureException.class)
    public void tamperedSignatureThrowsOnVerify() throws VerifyKey.SignatureException {
        byte[] signature = key.sign("bla".getBytes());
        signature[16] = 9;
        key.getVerifyKey().verify("bla".getBytes(), signature);
    }

    @Test(expected = VerifyKey.SignatureException.class)
    public void tamperedMessageThrowsOnVerify() throws VerifyKey.SignatureException {
        byte[] signature = key.sign("bla".getBytes());
        key.getVerifyKey().verify("bla9".getBytes(), signature);
    }

}
