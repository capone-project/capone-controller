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

import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.keys.VerifyKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

public class CapabilityToTest {

    @Test
    public void creatingReferenceSucceeds() {
        CapabilityTo root = new CapabilityTo(new byte[CapabilityTo.SECRET_LENGTH]);
        IdentityTo key = new IdentityTo(new VerifyKey(new byte[SodiumConstants.PUBLICKEY_BYTES]));
        CapabilityTo ref = root.createReference(
                CapabilityTo.RIGHT_EXEC | CapabilityTo.RIGHT_TERMINATE, key);

        Assert.assertEquals(Hex.toHexString(ref.secret),
                            "ef65d681590e6c95d923bb0da71851cc4902491f054ec767b8c3aa697df1c913");
    }

}
