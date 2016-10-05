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

package com.github.capone.services.invoke;

import com.github.capone.controller.R;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.services.ServicePlugin;
import com.github.capone.services.ServiceConfigurationFragment;

public class InvokeServicePlugin implements ServicePlugin {

    @Override
    public String getType() {
        return "invoke";
    }

    @Override
    public ServiceConfigurationFragment getConfigurationFragment(Server server, ServiceDescription service) {
        return InvokeConfigurationFragment.createFragment(server, service);
    }

    @Override
    public int getCategoryImageId(String category) {
        return R.drawable.service_invoke;
    }

}
