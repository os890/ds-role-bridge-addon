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
package org.os890.demo.role.bean;

import javax.annotation.security.RunAs;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@RunAs("x")
public class CdiRunAsX implements SimpleBean
{
    @Inject
    private CdiRestrictToX restrictToX;

    @Inject
    private CdiRestrictToY restrictToY;

    public String getValue()
    {
        StringBuilder result = new StringBuilder();
        result.append("x: ").append(getValueFrom(restrictToX)).append(" - y: ").append(getValueFrom(restrictToY));

        return result.toString();
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
