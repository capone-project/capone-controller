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

package com.github.capone.services;

import com.github.capone.controller.R;
import com.github.capone.entities.ServerTo;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.services.capabilities.CapabilityPluginFragment;
import com.github.capone.services.exec.ExecPluginFragment;
import com.github.capone.services.invoke.InvokePluginFragment;

public class Plugins {

    public static PluginFragment getPlugin(ServerTo server, ServiceDescriptionTo service) {
        switch (service.type) {
            case "invoke":
                return InvokePluginFragment.createFragment(server, service);
            case "exec":
                return ExecPluginFragment.createFragment(server, service);
            case "capabilities":
                return CapabilityPluginFragment.createFragment(server, service);
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
