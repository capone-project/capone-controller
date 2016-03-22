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

package im.pks.sd.controller.discovery;

import im.pks.sd.controller.R;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

public class Service implements Serializable {
    public String name;
    public String type;
    public int port;

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public int getResourceId() {
        switch (type) {
            case "Display":
                return R.drawable.service_display;
            case "Input":
                return R.drawable.service_input;
            case "Shell":
                return R.drawable.service_shell;
            case "Capabilities":
                return R.drawable.service_capabilities;
            default:
                return R.drawable.service_unknown;
        }
    }

}
