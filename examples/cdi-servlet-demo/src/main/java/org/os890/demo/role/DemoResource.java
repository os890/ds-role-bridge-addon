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
package org.os890.demo.role;

import org.os890.demo.role.bean.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("overview")
@ApplicationScoped
public class DemoResource
{
    private static final String SEPARATOR = "<p/>";

    @Inject
    private CdiPermitAll cdiPermitAll;

    @Inject
    private CdiDenyAll cdiDenyAll;

    @Inject
    private CdiRestrictToX restrictToX;

    @Inject
    private CdiRestrictToY restrictToY;

    @Inject
    private CdiRunAsX cdiRunAsX;

    @GET
    public String overview()
    {
        StringBuilder result = new StringBuilder();
        result.append("permit-all: ").append(getValueFrom(cdiPermitAll)).append(SEPARATOR);
        result.append("deny-all: ").append(getValueFrom(cdiDenyAll)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("protected by role x: ").append(getValueFrom(restrictToX)).append(SEPARATOR);
        result.append("protected by role y: ").append(getValueFrom(restrictToY)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("run-as x: ").append(getValueFrom(cdiRunAsX)).append(SEPARATOR);
        return "answer: " + result.toString();
    }

    private String getValueFrom(SimpleBean simpleBean)
    {
        try
        {
            return simpleBean.getValue();
        }
        catch (SecurityException e)
        {
            return "!restricted!";
        }
    }
}
