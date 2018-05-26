package org.os890.demo.role;

import org.os890.demo.role.bean.*;

import javax.inject.Inject;

public abstract class DemoBaseResource
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

    public String overview()
    {
        StringBuilder result = new StringBuilder();
        result.append("permit-all: ").append(getValueFrom(cdiPermitAll)).append(SEPARATOR);
        result.append("deny-all: ").append(getValueFrom(cdiDenyAll)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("protected by role x: ").append(getValueFrom(restrictToX)).append(SEPARATOR);
        result.append("protected by role y: ").append(getValueFrom(restrictToY)).append(SEPARATOR);
        result.append(SEPARATOR);
        result.append("run-as: ").append(getValueFrom(cdiRunAsX)).append(SEPARATOR);
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
