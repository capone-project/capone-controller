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
import im.pks.sd.controller.invoke.QueryResults;

public class Plugins {

    public static PluginFragment getPlugin(QueryResults service) {
        switch (service.type) {
            case "invoke":
                return InvokePluginFragment.createFragment(service);
            default:
                return GenericPluginFragment.createFragment(service);
        }
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
