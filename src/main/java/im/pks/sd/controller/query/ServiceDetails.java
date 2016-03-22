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

package im.pks.sd.controller.query;

import im.pks.sd.controller.discovery.Server;
import im.pks.sd.controller.discovery.Service;

import java.util.List;

public class ServiceDetails {

    public static class Parameter {
        public String name;
        public List<String> values;

        public Parameter(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }

    public final Server server;
    public final Service service;
    public final String subtype;
    public final String location;
    public final String version;
    public final List<Parameter> parameters;

    public ServiceDetails(Server server, Service service, String subtype, String location,
                          String version, List<Parameter> parameters) {
        this.server = server;
        this.service = service;
        this.subtype = subtype;
        this.location = location;
        this.version = version;
        this.parameters = parameters;
    }

}
