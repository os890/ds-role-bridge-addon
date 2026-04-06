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

package org.os890.cdi.addon.role.bridge.impl.interceptor;

import org.os890.cdi.addon.role.bridge.spi.RoleEvaluationStrategy;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;

/**
 * Interceptor that triggers class-level role evaluation for beans annotated
 * with security annotations such as {@code @RolesAllowed}, {@code @DenyAll}
 * or {@code @PermitAll}.
 */
@Interceptor
@RoleClassAdapterInterceptorBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class RoleClassAdapterInterceptor implements Serializable
{
    private static final long serialVersionUID = -3094673146532371976L;

    @Inject
    private RoleEvaluationStrategy roleEvaluationStrategy;

    /**
     * Delegates the invocation to the {@link RoleEvaluationStrategy} to
     * evaluate whether the caller has the required roles.
     *
     * @param invocationContext the intercepted invocation context
     * @return the result of the invocation if access is granted
     * @throws Exception if the invocation itself throws
     */
    @AroundInvoke
    public Object filterDeniedInvocations(InvocationContext invocationContext) throws Exception
    {
        return roleEvaluationStrategy.execute(invocationContext);
    }
}
