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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RunAsRoleStorage}.
 */
class RunAsRoleStorageTest
{
    private RunAsRoleStorage storage;

    @BeforeEach
    void setUp()
    {
        storage = new RunAsRoleStorage();
        // ensure clean thread-local state
        while (storage.isRunAsCall())
        {
            storage.onMethodReturn();
        }
    }

    @Test
    void initialStateIsNotRunAs()
    {
        assertFalse(storage.isRunAsCall());
    }

    @Test
    void addRolesActivatesRunAs()
    {
        storage.addRoles("admin");
        assertTrue(storage.isRunAsCall());
    }

    @Test
    void isInRunAsRoleMatchesCurrentRole()
    {
        storage.addRoles("admin");
        assertTrue(storage.isInRunAsRole("admin"));
        assertFalse(storage.isInRunAsRole("user"));
    }

    @Test
    void onMethodReturnClearsRunAs()
    {
        storage.addRoles("admin");
        storage.onMethodReturn();
        assertFalse(storage.isRunAsCall());
    }

    @Test
    void nestedRunAsSwitchesContext()
    {
        storage.addRoles("admin");
        storage.addRoles("user");

        assertTrue(storage.isInRunAsRole("user"));
        assertFalse(storage.isInRunAsRole("admin"));

        storage.onMethodReturn();
        assertTrue(storage.isInRunAsRole("admin"));

        storage.onMethodReturn();
        assertFalse(storage.isRunAsCall());
    }
}
