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

package org.os890.demo.role;

import org.os890.demo.role.bean.EjbDenyAll;
import org.os890.demo.role.bean.EjbPermitAll;
import org.os890.demo.role.bean.EjbRestrictToX;
import org.os890.demo.role.bean.EjbRestrictToY;
import org.os890.demo.role.bean.EjbRunAsX;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Abstract base resource that aggregates security-annotated EJBs and
 * produces an overview of their accessibility.
 */
public abstract class DemoBaseResource
{
    private static final String SEPARATOR = "<p/>";

    @EJB
    private EjbPermitAll ejbPermitAll;

    @EJB
    private EjbDenyAll ejbDenyAll;

    @EJB
    private EjbRestrictToX restrictToX;

    @EJB
    private EjbRestrictToY restrictToY;

    @EJB
    private EjbRunAsX ejbRunAsX;

    /**
     * Builds an overview string showing the result of invoking each
     * security-annotated EJB.
     *
     * @return the formatted overview string
     */
    public String overview()
    {
        StringBuilder result = new StringBuilder();
        result.append("permit-all: ").append(getValueFrom(ejbPermitAll)).append(SEPARATOR);
        result.append("deny-all: ").append(getValueFrom(ejbDenyAll)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("protected by role x: ").append(getValueFrom(restrictToX)).append(SEPARATOR);
        result.append("protected by role y: ").append(getValueFrom(restrictToY)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("run-as: ").append(getValueFrom(ejbRunAsX)).append(SEPARATOR);
        return "answer: " + result.toString();
    }

    private String getValueFrom(Object simpleBean)
    {
        try
        {
            Method method = simpleBean.getClass().getDeclaredMethod("getValue");
            return (String) method.invoke(simpleBean);
        }
        catch (EJBAccessException e)
        {
            return "!restricted!";
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof EJBAccessException)
            {
                return "!restricted!";
            }
            return "!error!";
        }
        catch (Exception e)
        {
            return "!error!";
        }
    }
}
