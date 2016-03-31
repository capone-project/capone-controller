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

public class InvokePlugin implements Plugin {

    @Override
    public String getType() {
        return "invoke";
    }

    @Override
    public int getImageId() {
        return R.drawable.service_invoke;
    }

    @Override
    public PluginFragment getFragment(QueryResults service) {
        return InvokePluginFragment.createFragment(service);
    }

    @Override
    public PluginTask getTask(PluginFragment fragment) {
        InvokePluginFragment invokeFragment = (InvokePluginFragment) fragment;
        return invokeFragment.createTask();
    }

}
