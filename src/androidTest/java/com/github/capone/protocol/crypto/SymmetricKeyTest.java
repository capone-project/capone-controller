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
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SymmetricKeyTest {

    private SymmetricKey key;
    private SymmetricKey.Nonce nonce;

    @Before
    public void setUp() throws SymmetricKey.InvalidKeyException {
        key = SymmetricKey.fromBytes(new byte[SymmetricKey.BYTES]);
        nonce = new SymmetricKey.Nonce();
    }

    @Test
    @Ignore
    public void creatingKeySucceeds() throws SymmetricKey.InvalidKeyException {
        key = SymmetricKey.fromBytes(new byte[SymmetricKey.BYTES]);
        Assert.assertNotNull(key);
        Assert.assertEquals("00000000000000000000000000000000", key.toString());
    }

    @Test(expected = SymmetricKey.InvalidKeyException.class)
    public void creatingKeyWithInvalidLengthFails() throws SymmetricKey.InvalidKeyException {
        key = SymmetricKey.fromBytes(new byte[SymmetricKey.BYTES + 1]);
    }

    @Test
    public void creatingKeyFromScalarMultiplicationSucceeds()
            throws SymmetricKey.InvalidKeyException, SymmetricKey.EncryptionException, SymmetricKey.DecryptionException {
        PrivateKey alice = PrivateKey.fromRandom();
        PrivateKey bob = PrivateKey.fromRandom();

        SymmetricKey alicesKey = SymmetricKey.fromScalarMultiplication(alice, bob.getPublicKey(),
                                                                       true);
        SymmetricKey bobsKey = SymmetricKey.fromScalarMultiplication(bob, alice.getPublicKey(),
                                                                     false);

        byte[] cipher = alicesKey.encrypt(nonce, "test".getBytes());
        Assert.assertEquals("test", new String(bobsKey.decrypt(nonce, cipher)));
    }

    @Test
    public void encryptionIsStable()
            throws SymmetricKey.InvalidKeyException, SymmetricKey.EncryptionException {
        byte[] cipher = key.encrypt(nonce, "bla".getBytes());
        Assert.assertEquals("a6f0bbe1c6553ef5c5211f95bccaa5a2a452da", Hex.toHexString(cipher));
    }

    @Test
    public void encryptionIsReversable()
            throws SymmetricKey.EncryptionException, SymmetricKey.DecryptionException {
        byte[] cipher = key.encrypt(nonce, "bla".getBytes());
        byte[] plain = key.decrypt(nonce, cipher);
        Assert.assertEquals("bla", new String(plain));
    }

    @Test(expected = SymmetricKey.DecryptionException.class)
    public void tamperedCiphertextCausesException()
            throws SymmetricKey.EncryptionException, SymmetricKey.DecryptionException {
        byte[] cipher = key.encrypt(nonce, "bla".getBytes());
        cipher[7] = 7;
        key.decrypt(nonce, cipher);
    }

    @Test
    public void incrementingNonceChangesMessage() throws SymmetricKey.EncryptionException {
        byte[] cipher = key.encrypt(nonce, "bla".getBytes());
        nonce.increment();
        byte[] other = key.encrypt(nonce, "bla".getBytes());
        Assert.assertNotEquals(Hex.toHexString(cipher), Hex.toHexString(other));
    }

    @Test(expected = SymmetricKey.DecryptionException.class)
    public void decryptingWithWrongNonceFails()
            throws SymmetricKey.EncryptionException, SymmetricKey.DecryptionException {
        byte[] cipher = key.encrypt(nonce, "bla".getBytes());
        nonce.increment();
        key.decrypt(nonce, cipher);
    }

}
