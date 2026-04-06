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

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Stack;

/**
 * Thread-local storage for {@code @RunAs} roles.
 * Maintains a per-thread stack so that nested {@code @RunAs} invocations
 * are handled correctly (context-switch semantics).
 */
@ApplicationScoped
public class RunAsRoleStorage
{
    private static final ThreadLocal<Stack<String>> RUN_AS_ROLE_STACK = new ThreadLocal<>();

    /**
     * Returns whether there is currently an active {@code @RunAs} invocation
     * on the calling thread.
     *
     * @return {@code true} if a run-as role is active
     */
    public boolean isRunAsCall()
    {
        Stack<String> runAsRoleStack = RUN_AS_ROLE_STACK.get();
        return runAsRoleStack != null && !runAsRoleStack.isEmpty();
    }

    /**
     * Checks whether the given role matches the currently active
     * {@code @RunAs} role (top of the stack).
     *
     * @param roleName the role to check
     * @return {@code true} if the role matches the current run-as context
     */
    public boolean isInRunAsRole(String roleName)
    {
        Stack<String> runAsRoleStack = RUN_AS_ROLE_STACK.get();
        return runAsRoleStack != null && !runAsRoleStack.isEmpty() && roleName.equals(runAsRoleStack.peek());

        //use the following to aggregate roles in case of nested @RunAs cases (instead of a context-switch)
        //return runAsRoleStack != null && runAsRoleStack.contains(roleName);
    }

    /**
     * Pushes a new run-as role onto the thread-local stack.
     *
     * @param roleName the role to push
     */
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

    /**
     * Pops the most recent run-as role from the thread-local stack.
     * Cleans up the thread-local when the stack becomes empty.
     */
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
