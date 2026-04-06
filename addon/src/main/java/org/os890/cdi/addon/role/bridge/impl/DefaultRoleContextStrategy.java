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
import org.os890.cdi.addon.role.bridge.spi.RoleContextStrategy;

import jakarta.annotation.security.RunAs;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;

import static org.os890.cdi.addon.role.bridge.impl.util.AnnotationUtils.extractAnnotationFromClass;

/**
 * Default {@link RoleContextStrategy} that pushes the {@code @RunAs} role onto
 * the {@link RunAsRoleStorage} before proceeding with the invocation and pops
 * it on return.
 */
// CDI proxies are serializable at runtime; suppress compiler false positives.
@SuppressWarnings("serial")
@Dependent
public class DefaultRoleContextStrategy implements RoleContextStrategy
{
    private static final long serialVersionUID = 1L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private RunAsRoleStorage runAsRoleStorage;

    /**
     * Executes the intercepted invocation within the context of the
     * {@code @RunAs} role declared on the target class.
     *
     * @param invocationContext the intercepted invocation context
     * @return the result of the invocation
     * @throws Exception if the invocation itself throws
     */
    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        try
        {
            Class<?> targetClass = invocationContext.getMethod().getDeclaringClass();
            targetClass = ProxyUtils.getUnproxiedClass(targetClass);

            RunAs runAs = extractAnnotationFromClass(beanManager, targetClass, RunAs.class);
            runAsRoleStorage.addRoles(runAs.value());

            return invocationContext.proceed();
        }
        finally
        {
            runAsRoleStorage.onMethodReturn();
        }
    }
}
