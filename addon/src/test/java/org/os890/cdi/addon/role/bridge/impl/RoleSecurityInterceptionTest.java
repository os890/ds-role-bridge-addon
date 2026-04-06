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

import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.os890.cdi.addon.dynamictestbean.EnableTestBeans;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CDI SE integration tests that verify the interception of Java EE security
 * annotations ({@code @RolesAllowed}, {@code @PermitAll}, {@code @DenyAll},
 * {@code @RunAs}) by the role-bridge interceptors.
 *
 * <p>A test-scoped {@link ControllableRoleEvaluator} replaces the
 * {@link org.os890.cdi.addon.role.bridge.impl.DefaultRoleEvaluator} via
 * the CDI {@code @Alternative} mechanism so that role membership can be
 * controlled from the test methods.</p>
 */
@EnableTestBeans
class RoleSecurityInterceptionTest
{
    @Inject
    private SecuredBean securedBean;

    @Inject
    private RunAsBean runAsBean;

    @Inject
    private ControllableRoleEvaluator roleEvaluator;

    @Inject
    private RunAsRoleStorage runAsRoleStorage;

    /**
     * Resets the controllable role evaluator after every test to prevent
     * cross-test pollution.
     */
    @AfterEach
    void resetRoles()
    {
        roleEvaluator.clearRoles();
    }

    // ------------------------------------------------------------------ @RolesAllowed

    /**
     * Verifies that a method annotated with {@code @RolesAllowed("admin")}
     * is accessible when the caller holds the {@code admin} role.
     */
    @Test
    void rolesAllowedGrantsAccessWhenRoleMatches()
    {
        roleEvaluator.grantRole("admin");

        String result = securedBean.adminOnly();

        assertEquals("admin-result", result);
    }

    /**
     * Verifies that a method annotated with {@code @RolesAllowed("admin")}
     * throws a {@link SecurityException} when the caller does not hold the
     * required role.
     */
    @Test
    void rolesAllowedDeniesAccessWhenRoleMissing()
    {
        roleEvaluator.grantRole("user");

        assertThrows(SecurityException.class, () -> securedBean.adminOnly());
    }

    /**
     * Verifies that a method annotated with {@code @RolesAllowed("admin")}
     * throws a {@link SecurityException} when the caller holds no roles
     * at all.
     */
    @Test
    void rolesAllowedDeniesAccessWhenNoRolesGranted()
    {
        assertThrows(SecurityException.class, () -> securedBean.adminOnly());
    }

    // ------------------------------------------------------------------ @PermitAll

    /**
     * Verifies that a method annotated with {@code @PermitAll} is always
     * accessible, even when the caller holds no roles.
     */
    @Test
    void permitAllGrantsAccessWithoutRoles()
    {
        String result = securedBean.openAccess();

        assertEquals("open-result", result);
    }

    /**
     * Verifies that a method annotated with {@code @PermitAll} is accessible
     * when the caller happens to hold roles (roles are irrelevant).
     */
    @Test
    void permitAllGrantsAccessWithRoles()
    {
        roleEvaluator.grantRole("admin");

        String result = securedBean.openAccess();

        assertEquals("open-result", result);
    }

    // ------------------------------------------------------------------ @DenyAll

    /**
     * Verifies that a method annotated with {@code @DenyAll} always throws
     * a {@link SecurityException}, even when the caller holds roles.
     */
    @Test
    void denyAllDeniesAccessEvenWithRoles()
    {
        roleEvaluator.grantRole("admin");

        assertThrows(SecurityException.class, () -> securedBean.noAccess());
    }

    /**
     * Verifies that a method annotated with {@code @DenyAll} throws a
     * {@link SecurityException} when the caller holds no roles.
     */
    @Test
    void denyAllDeniesAccessWithoutRoles()
    {
        assertThrows(SecurityException.class, () -> securedBean.noAccess());
    }

    // ------------------------------------------------------------------ @RunAs

    /**
     * Verifies that a bean annotated with {@code @RunAs("admin")} causes the
     * {@link RunAsRoleStorage} to report the {@code admin} role as active
     * during method execution.
     */
    @Test
    void runAsSetsRoleContextDuringExecution()
    {
        String capturedRole = runAsBean.captureCurrentRunAsRole();

        assertEquals("admin", capturedRole);
    }

    /**
     * Verifies that the {@code @RunAs} role context is cleaned up after the
     * method returns so it does not leak into subsequent invocations.
     */
    @Test
    void runAsCleansUpAfterMethodReturns()
    {
        runAsBean.captureCurrentRunAsRole();

        boolean stillActive = runAsRoleStorage.isRunAsCall();

        assertTrue(!stillActive, "RunAs context should be cleared after method return");
    }

    // ------------------------------------------------------------------ test beans

    /**
     * Application-scoped bean with methods secured by various Java EE
     * security annotations. Used as the target of interception tests.
     */
    @ApplicationScoped
    public static class SecuredBean
    {
        /**
         * Method that requires the {@code admin} role.
         *
         * @return a fixed string identifying the method
         */
        @RolesAllowed("admin")
        public String adminOnly()
        {
            return "admin-result";
        }

        /**
         * Method accessible to all callers.
         *
         * @return a fixed string identifying the method
         */
        @PermitAll
        public String openAccess()
        {
            return "open-result";
        }

        /**
         * Method that denies all callers unconditionally.
         *
         * @return never returns normally
         */
        @DenyAll
        public String noAccess()
        {
            return "denied-result";
        }
    }

    /**
     * Application-scoped bean annotated with {@code @RunAs("admin")} so that
     * the {@link RunAsInterceptor} establishes the admin role context around
     * every invocation.
     */
    @ApplicationScoped
    @RunAs("admin")
    public static class RunAsBean
    {
        @Inject
        private RunAsRoleStorage runAsRoleStorage;

        /**
         * Captures and returns the currently active {@code @RunAs} role,
         * or {@code "none"} if no run-as context is active.
         *
         * @return the current run-as role name
         */
        public String captureCurrentRunAsRole()
        {
            if (runAsRoleStorage.isRunAsCall())
            {
                if (runAsRoleStorage.isInRunAsRole("admin"))
                {
                    return "admin";
                }
                return "unknown";
            }
            return "none";
        }
    }

    /**
     * Test {@link RoleEvaluator} implementation that replaces the
     * {@link DefaultRoleEvaluator} via {@code @Alternative} with a high
     * priority. The set of granted roles can be controlled from test methods
     * via {@link #grantRole(String)} and {@link #clearRoles()}.
     */
    @Alternative
    @Priority(Interceptor.Priority.APPLICATION + 100)
    @ApplicationScoped
    public static class ControllableRoleEvaluator implements RoleEvaluator
    {
        private final Set<String> grantedRoles = ConcurrentHashMap.newKeySet();

        /**
         * Grants the given role for subsequent security checks.
         *
         * @param role the role to grant
         */
        public void grantRole(String role)
        {
            grantedRoles.add(role);
        }

        /**
         * Removes all previously granted roles.
         */
        public void clearRoles()
        {
            grantedRoles.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUserInRole(String roleName)
        {
            return grantedRoles.contains(roleName);
        }
    }
}
