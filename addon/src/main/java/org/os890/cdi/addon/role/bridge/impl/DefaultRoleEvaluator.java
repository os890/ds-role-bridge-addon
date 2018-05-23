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

import org.apache.deltaspike.core.api.literal.DeltaSpikeLiteral;
import org.apache.deltaspike.core.util.ClassUtils;
import org.os890.cdi.addon.role.bridge.spi.RoleEvaluator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.servlet.http.HttpServletRequest;

import static org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference;

@ApplicationScoped
public class DefaultRoleEvaluator implements RoleEvaluator
{
    //allows to use it also in a container which doesn't support both (tested e.g. with apache-meecrowave)
    private String HTTP_SERVLET_REQUEST_CLASS_NAME = "javax.servlet.http.HttpServletRequest";
    private String EJB_CONTEXT_CLASS_NAME = "javax.ejb.EJBContext";

    private Class<?> servletRequestClass;
    private Class<?> ejbContextClass;

    @PostConstruct
    protected void init()
    {
        servletRequestClass = ClassUtils.tryToLoadClassForName(HTTP_SERVLET_REQUEST_CLASS_NAME);
        ejbContextClass = ClassUtils.tryToLoadClassForName(EJB_CONTEXT_CLASS_NAME);
    }

    @Override
    public boolean isUserInRole(String roleName)
    {
        Boolean result = tryToCheckForRequest(roleName);

        if (result == null)
        {
            result = tryToCheckInEjbContext(roleName);
        }

        if (result == null)
        {
            throw new IllegalStateException("role-check outside a servlet-request or ejb-context detected");
        }

        return result;
    }

    private Boolean tryToCheckForRequest(String roleName)
    {
        if (servletRequestClass == null)
        {
            return null;
        }

        HttpServletRequest servletRequest = getContextualReference(HttpServletRequest.class, true);
        if (servletRequest == null)
        {
            servletRequest = getContextualReference(HttpServletRequest.class, true, new DeltaSpikeLiteral());
        }

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
        return null;
    }

    private Boolean tryToCheckInEjbContext(String roleName)
    {
        if (ejbContextClass == null)
        {
            return null;
        }
        return getContextualReference(EjbRoleHelper.class).isUserInRole(roleName);
    }
}
