/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.os890.cdi.addon.role.bridge.impl;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.os890.cdi.addon.dynamictestbean.EnableTestBeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CDI SE integration test that verifies the {@link RunAsRoleStorage} bean
 * is resolvable and functional in a CDI SE environment.
 */
@EnableTestBeans
class RoleAdapterExtensionCdiTest
{
    @Inject
    private Instance<RunAsRoleStorage> runAsRoleStorageInstance;

    /**
     * Verifies that {@link RunAsRoleStorage} is discoverable as a CDI bean.
     */
    @Test
    void runAsRoleStorageIsResolvable()
    {
        assertNotNull(runAsRoleStorageInstance);
        assertFalse(runAsRoleStorageInstance.isUnsatisfied(),
                "RunAsRoleStorage should be resolvable as a CDI bean");
    }

    /**
     * Verifies that the injected {@link RunAsRoleStorage} instance is functional.
     */
    @Test
    void runAsRoleStorageInstanceIsAccessible()
    {
        RunAsRoleStorage storage = runAsRoleStorageInstance.get();
        assertNotNull(storage);
        assertFalse(storage.isRunAsCall());
    }
}
