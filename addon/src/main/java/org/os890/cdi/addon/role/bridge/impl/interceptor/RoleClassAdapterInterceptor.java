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
package org.os890.cdi.addon.role.bridge.impl.interceptor;

import org.os890.cdi.addon.role.bridge.spi.RoleEvaluationStrategy;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@RoleClassAdapterInterceptorBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class RoleClassAdapterInterceptor implements Serializable
{
    private static final long serialVersionUID = -3094673146532371976L;

    @Inject
    private RoleEvaluationStrategy roleEvaluationStrategy;

    @AroundInvoke
    public Object filterDeniedInvocations(InvocationContext invocationContext) throws Exception
    {
        return roleEvaluationStrategy.execute(invocationContext);
    }
}
