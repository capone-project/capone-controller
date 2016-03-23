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

package im.pks.sd.persistence;

import com.orm.SugarRecord;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.SigningKey;

public class Identity extends SugarRecord {

    private String keySeed;

    public Identity() {
    }

    public Identity(SigningKey key) {
        this.keySeed = key.toString();
    }

    public SigningKey getKey() {
        return new SigningKey(keySeed, Encoder.HEX);
    }

    public void setKey(SigningKey key) {
        this.keySeed = key.toString();
    }

}