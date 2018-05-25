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

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.inject.Vetoed;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

//!!! allows to define the (principal-)roles dynamically to easily check the different constellations in the browser
@Vetoed
class DynamicTestRealm extends RealmBase
{
    private String username;
    private String credentials;

    @Override
    public Principal authenticate(String username, String credentials)
    {
        this.username = username;
        this.credentials = credentials;
        return super.authenticate(username, credentials);
    }

    @Override
    protected String getPassword(String username)
    {
        return this.credentials;
    }

    @Override
    protected Principal getPrincipal(String username)
    {
        TestGroupStorage testGroupStorage = BeanProvider.getContextualReference(TestGroupStorage.class);
        List<String> roles = new ArrayList<>(testGroupStorage.getTestGroups());

        return new GenericPrincipal(this.username, this.credentials, roles);
    }
}
