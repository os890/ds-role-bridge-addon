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

import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.os890.cdi.addon.role.bridge.spi.AccessDeniedHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

@ApplicationScoped
public class DefaultAccessDeniedHandler implements AccessDeniedHandler
{
    @Inject
    private BeanManager beanManager;

    @Override
    public Object handleAccessDenied(InvocationContext invocationContext)
    {
        Method targetMethod = invocationContext.getMethod();
        Class<?> targetClass = targetMethod.getDeclaringClass();

        SecurityException securityException = new SecurityException(
            "access denied to target: " + targetClass.getName() + "#" + targetMethod.getName());

        beanManager.fireEvent(new ExceptionToCatchEvent(securityException));
        throw securityException; //we shouldn't get here, because the ds-exception-control will throw it after processing the event
    }
}
