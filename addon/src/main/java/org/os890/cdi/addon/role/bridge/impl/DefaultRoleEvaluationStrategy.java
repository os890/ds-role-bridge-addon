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

import org.apache.deltaspike.core.util.ProxyUtils;
import org.os890.cdi.addon.role.bridge.spi.AccessDeniedHandler;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluationStrategy;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluator;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;

import static org.apache.deltaspike.core.util.AnnotationUtils.extractAnnotationFromMethod;
import static org.os890.cdi.addon.role.bridge.impl.util.AnnotationUtils.extractAnnotationFromClass;

/**
 * Default {@link RoleEvaluationStrategy} that evaluates {@code @DenyAll},
 * {@code @PermitAll} and {@code @RolesAllowed} annotations at method and
 * class level to decide whether the invocation should proceed.
 */
// CDI proxies are serializable at runtime; suppress compiler false positives.
@SuppressWarnings("serial")
@Dependent
public class DefaultRoleEvaluationStrategy implements RoleEvaluationStrategy
{
    private static final long serialVersionUID = 1L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private RoleEvaluator roleEvaluator;

    @Inject
    private AccessDeniedHandler accessDeniedHandler;

    /**
     * Evaluates security annotations on the target class and method, proceeding
     * with the invocation if the caller has the required roles, or delegating
     * to the {@link AccessDeniedHandler} otherwise.
     *
     * @param invocationContext the intercepted invocation context
     * @return the result of the invocation or the handler result
     * @throws Exception if the invocation itself throws
     */
    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Class<?> targetClass = invocationContext.getMethod().getDeclaringClass();
        targetClass = ProxyUtils.getUnproxiedClass(targetClass);

        boolean roleAllowedResult = checkForAllowedRoles(targetClass, invocationContext.getMethod());
        if (roleAllowedResult)
        {
            return invocationContext.proceed();
        }

        return accessDeniedHandler.handleAccessDenied(invocationContext);
    }

    /**
     * Checks method-level and then class-level security annotations to determine
     * whether the call is allowed.
     *
     * @param targetClass  the unproxied target class
     * @param targetMethod the target method
     * @return {@code true} if the call is allowed
     */
    protected boolean checkForAllowedRoles(Class<?> targetClass, Method targetMethod)
    {
        //check method-level

        DenyAll denyAll = extractAnnotationFromMethod(beanManager, targetMethod, DenyAll.class);
        if (denyAll != null)
        {
            return false;
        }

        PermitAll permitAll = extractAnnotationFromMethod(beanManager, targetMethod, PermitAll.class);
        if (permitAll != null)
        {
            return true;
        }

        RolesAllowed rolesAllowed = extractAnnotationFromMethod(beanManager, targetMethod, RolesAllowed.class);
        if (rolesAllowed != null)
        {
            return checkForAllowedRoles(rolesAllowed);
        }

        //check class-level
        denyAll = extractAnnotationFromClass(beanManager, targetClass, DenyAll.class);
        if (denyAll != null)
        {
            return false;
        }

        rolesAllowed = extractAnnotationFromClass(beanManager, targetClass, RolesAllowed.class);
        if (rolesAllowed != null)
        {
            return checkForAllowedRoles(rolesAllowed);
        }

        permitAll = extractAnnotationFromClass(beanManager, targetClass, PermitAll.class);
        return permitAll != null;
    }

    /**
     * Checks whether the current caller holds at least one of the roles
     * specified in the {@code @RolesAllowed} annotation.
     *
     * @param rolesAllowed the roles-allowed annotation
     * @return {@code true} if the caller holds at least one of the roles
     */
    protected boolean checkForAllowedRoles(RolesAllowed rolesAllowed)
    {
        for (String roleName : rolesAllowed.value())
        {
            if (roleEvaluator.isUserInRole(roleName))
            {
                return true;
            }
        }
        return false;
    }
}
