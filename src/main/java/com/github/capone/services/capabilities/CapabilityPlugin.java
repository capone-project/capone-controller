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

package com.github.capone.services.capabilities;

import com.github.capone.controller.R;
import com.github.capone.protocol.entities.ServerTo;
import com.github.capone.protocol.entities.ServiceDescriptionTo;
import com.github.capone.services.Plugin;
import com.github.capone.services.PluginFragment;

public class CapabilityPlugin implements Plugin {

    @Override
    public String getType() {
        return "capabilities";
    }

    @Override
    public PluginFragment getFragment(ServerTo server, ServiceDescriptionTo service) {
        return CapabilityPluginFragment.createFragment(server, service);
    }

    @Override
    public int getCategoryImageId(String category) {
        return R.drawable.service_capabilities;
    }

}
