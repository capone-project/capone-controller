/*
2 hours ago
submitgit commented on pull request git/git#219
@submitgit

    @pranitbauva1997 sent this commit (7e4ba36...a2fa85d) as a patch to the mailing list with submitGit - here on Gmane, MARC

2 hours ago
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

package im.pks.sd.entities;

import java.io.Serializable;
import java.util.List;

public class ServiceDescriptionTo implements Serializable {

    public static class Parameter implements Serializable {
        public String name;
        public String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public final ServerTo server;
    public final ServiceTo service;
    public final String type;
    public final String location;
    public final String version;
    public final List<Parameter> parameters;

    public ServiceDescriptionTo(ServerTo server, ServiceTo service, String type, String location,
                                String version, List<Parameter> parameters) {
        this.server = server;
        this.service = service;
        this.type = type;
        this.location = location;
        this.version = version;
        this.parameters = parameters;
    }

}
