package org.os890.demo.role;

import org.os890.demo.role.bean.*;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
