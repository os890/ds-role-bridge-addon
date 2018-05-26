/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.os890.demo.role;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.undertow.WARArchive;

//tested with intellij
//!!!but it's needed to open the demo-app project and not the whole project

//use e.g.:

//http://localhost:8080/cdi-ejb/demo/overview
//... for demo-user without roles

//http://localhost:8080/cdi-ejb/demo/overviewRoleX
//... for demo-user with role 'x'

//http://localhost:8080/cdi-ejb/demo/overviewRoleY
//... for demo-user with role 'y'

//http://localhost:8080/cdi-ejb/demo/overviewRoleZ
//... for demo-user with role 'z'
public class DynamicRoleTestStarter {
    public static void main(String[] args) throws Exception {
        Swarm container = new Swarm()
            .fraction(new ManagementFraction()
                .securityRealm("ManagementRealm", (realm) -> {
                    realm.inMemoryAuthentication((authn) -> authn.add("demo", "demo", true));
                    realm.inMemoryAuthorization((authz) -> authz.add("demo", "x", "y"));
                })
            );

        container.start();

        WARArchive warArchive = container.createDefaultDeployment().as(WARArchive.class).setContextRoot("cdi-ejb");
        container.deploy(warArchive);
    }
}
