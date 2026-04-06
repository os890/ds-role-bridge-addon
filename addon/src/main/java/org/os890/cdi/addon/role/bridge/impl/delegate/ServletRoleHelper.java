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

package org.os890.cdi.addon.role.bridge.impl.delegate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.servlet.http.HttpServletRequest;

import static org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference;

/**
 * Helper bean that performs role checks against the current
 * {@link HttpServletRequest}.
 */
@ApplicationScoped
public class ServletRoleHelper
{
    /**
     * Checks whether the current servlet request principal holds the given role.
     *
     * @param roleName the role to check
     * @return {@code true} if the user holds the role, {@code false} if not,
     *         or {@code null} if no servlet request is available
     */
    public Boolean isUserInRole(String roleName)
    {
        // DeltaSpikeLiteral fallback lookup was removed in DeltaSpike 2.x; use BeanProvider directly
        HttpServletRequest servletRequest = getContextualReference(HttpServletRequest.class, true);

        try
        {
            if (servletRequest != null)
            {
                return servletRequest.isUserInRole(roleName);
            }
        }
        catch (ContextNotActiveException e)
        {
            //do nothing - it was just a try -> next step is the fallback to the ejb-(context-)helper
        }
        return null; //triggers the next step (e.g. the ejb-fallback - if available)
    }
}
