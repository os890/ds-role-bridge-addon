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

import javax.enterprise.context.ApplicationScoped;
import java.util.Stack;

@ApplicationScoped
public class RunAsRoleStorage
{
    private static final ThreadLocal<Stack<String>> RUN_AS_ROLE_STACK = new ThreadLocal<>();

    public boolean isInRunAsRole(String roleName)
    {
        Stack<String> runAsRoleStack = RUN_AS_ROLE_STACK.get();
        return runAsRoleStack != null && !runAsRoleStack.isEmpty() && roleName.equals(runAsRoleStack.peek());

        //use the following to aggregate roles in case of nested @RunAs cases (instead of a context-switch)
        //return runAsRoleStack != null && runAsRoleStack.contains(roleName);
    }

    public void addRoles(String roleName)
    {
        Stack<String> runAsRoleStack = RUN_AS_ROLE_STACK.get();

        if (runAsRoleStack == null)
        {
            runAsRoleStack = new Stack<>(); //thread-safe because it's stored in the ThreadLocal
            RUN_AS_ROLE_STACK.set(runAsRoleStack);
        }

        runAsRoleStack.push(roleName);
    }

    public void onMethodReturn()
    {
        Stack<String> runAsRoleStack = RUN_AS_ROLE_STACK.get();

        if (runAsRoleStack != null)
        {
            runAsRoleStack.pop();

            if (runAsRoleStack.isEmpty())
            {
                RUN_AS_ROLE_STACK.set(null);
                RUN_AS_ROLE_STACK.remove(); //just needed for some jvms
            }
        }
    }
}
