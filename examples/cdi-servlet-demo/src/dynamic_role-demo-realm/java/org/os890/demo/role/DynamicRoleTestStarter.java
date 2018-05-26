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

import org.apache.meecrowave.Meecrowave;

//use e.g.:

//http://localhost:8080/cdi/demo/overview
//... for demo-user without roles

//http://localhost:8080/cdi/demo/overviewRoleX
//... for demo-user with role 'x' (via @RunAs)

//http://localhost:8080/cdi/demo/overviewRoleY
//... for demo-user with role 'y' (via @RunAs)

//http://localhost:8080/cdi/demo/overviewRoleZ
//... for demo-user with role 'z' (via @RunAs)



//for testing users with different-roles (without restarts and without @RunWith):

//http://localhost:8080/cdi/demo/overview?testWithGroups=x
//... for demo-user with role 'x'

//http://localhost:8080/cdi/demo/overview?testWithGroups=x,y
//... for demo-user with role 'x' and 'y'

//http://localhost:8080/cdi/demo/overview?testWithGroups=z
//... for demo-user with role 'z'
public class DynamicRoleTestStarter
{
    public static void main(String[] args)
    {
        Meecrowave.Builder builder = new Meecrowave.Builder();
        builder.realm(new DynamicTestRealm());

        new Meecrowave(builder).bake().await();
    }
}
