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
public class DigestTest {

    @Test
    public void emptyDigestRemainsConstant() {
        byte[] digest = new Digest().digest();
        Assert.assertEquals("0e5751c026e543b2e8ab2eb06099daa1d1e5df47778f7787faab45cdf12fe3a8",
                            Hex.toHexString(digest));
    }

    @Test
    public void digestWithDataIsConstant() {
        byte[] digest = new Digest().update("bla".getBytes()).digest();
        Assert.assertEquals("2bb07a8684ee3986e0aecba3d3c364e498c092debc2645ffaa0278e45d6b69b0",
                            Hex.toHexString(digest));
    }

}
