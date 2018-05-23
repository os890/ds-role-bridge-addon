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
package org.os890.cdi.addon.role.bridge.impl;

import org.apache.deltaspike.core.util.ProxyUtils;
import org.os890.cdi.addon.role.bridge.spi.AccessDeniedHandler;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluationStrategy;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluator;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

import static org.apache.deltaspike.core.util.AnnotationUtils.extractAnnotationFromMethod;
import static org.os890.cdi.addon.role.bridge.impl.util.AnnotationUtils.extractAnnotationFromClass;

@Dependent
public class DefaultRoleEvaluationStrategy implements RoleEvaluationStrategy
{
    @Inject
    private BeanManager beanManager;

    @Inject
    private RoleEvaluator roleEvaluator;

    @Inject
    private AccessDeniedHandler accessDeniedHandler;

    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Class targetClass = invocationContext.getMethod().getDeclaringClass();
        targetClass = ProxyUtils.getUnproxiedClass(targetClass);

        boolean roleAllowedResult = checkForAllowedRoles(targetClass, invocationContext.getMethod());
        if (roleAllowedResult)
        {
            return invocationContext.proceed();
        }

        return accessDeniedHandler.handleAccessDenied(invocationContext);
    }

    protected boolean checkForAllowedRoles(Class targetClass, Method targetMethod)
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
