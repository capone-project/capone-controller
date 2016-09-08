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
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.services.capabilities.CapabilityPluginFragment;
import im.pks.sd.services.exec.ExecPluginFragment;
import im.pks.sd.services.invoke.InvokePluginFragment;

public class Plugins {

    public static PluginFragment getPlugin(ServiceDescriptionTo service) {
        switch (service.type) {
            case "invoke":
                return InvokePluginFragment.createFragment(service);
            case "exec":
                return ExecPluginFragment.createFragment(service);
            case "capabilities":
                return CapabilityPluginFragment.createFragment(service);
            default:
                return null;
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
