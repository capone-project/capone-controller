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

package im.pks.sd.services;

import im.pks.sd.controller.R;
import im.pks.sd.controller.query.ServiceDetails;

import java.util.HashMap;
import java.util.Map;

public class Plugins {

    private static final Map<String, Plugin> plugins = new HashMap<>();
    private static final Plugin fallback = new GenericPlugin();

    static {
        InvokePlugin invokePlugin = new InvokePlugin();
        plugins.put(invokePlugin.getType(), invokePlugin);
    }

    public static Plugin getPlugin(ServiceDetails service) {
        if (plugins.containsKey(service.subtype)) {
            return plugins.get(service.subtype);
        }

        return fallback;
    }

    public static int getCategoryImageId(String category) {
        switch (category) {
            case "Capabilities":
                return R.drawable.service_capabilities;
            case "Display":
                return R.drawable.service_display;
            case "Input":
                return R.drawable.service_input;
            case "Invoke":
                return R.drawable.service_invoke;
            case "Shell":
                return R.drawable.service_shell;
            default:
                return R.drawable.service_unknown;
        }
    }

}
