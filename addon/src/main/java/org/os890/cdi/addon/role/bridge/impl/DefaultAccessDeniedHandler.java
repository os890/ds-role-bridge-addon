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

import org.os890.cdi.addon.role.bridge.spi.AccessDeniedHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * Default {@link AccessDeniedHandler} that throws a {@link SecurityException}
 * when access to the target method is denied.
 */
@ApplicationScoped
public class DefaultAccessDeniedHandler implements AccessDeniedHandler
{
    /**
     * Handles an access-denied situation by throwing a {@link SecurityException}.
     *
     * @param invocationContext the intercepted invocation context
     * @return never returns normally
     * @throws SecurityException always thrown to signal the denied access
     */
    @Override
    public Object handleAccessDenied(InvocationContext invocationContext)
    {
        Method targetMethod = invocationContext.getMethod();
        Class<?> targetClass = targetMethod.getDeclaringClass();

        // ExceptionToCatchEvent was removed in DeltaSpike 2.x; throw directly instead of firing the event
        SecurityException securityException = new SecurityException(
            "access denied to target: " + targetClass.getName() + "#" + targetMethod.getName());

        throw securityException;
    }
}
