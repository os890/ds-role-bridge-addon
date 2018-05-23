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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RoleClassAdapterInterceptorBinding;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RoleMethodAdapterInterceptorBinding;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RunAsInterceptorBinding;

import javax.annotation.security.RunAs;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RoleAdapterExtension implements Extension, Deactivatable
{
    private static final String EJB_PACKAGE_NAME = "javax.ejb.";

    private static Set<Class> roleAnnotationClasses = new HashSet<Class>();

    static
    {
        roleAnnotationClasses.add(javax.annotation.security.DenyAll.class);
        roleAnnotationClasses.add(javax.annotation.security.PermitAll.class);
        roleAnnotationClasses.add(javax.annotation.security.RolesAllowed.class);
    }

    private Boolean isActivated = true;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    public <X> void checkForAuthenticationAnnotations(@Observes ProcessAnnotatedType<X> pat)
    {
        if (!isActivated)
        {
            return;
        }

        Class<?> beanClass = pat.getAnnotatedType().getJavaClass();

        checkForRoleAnnotations(pat, beanClass);
    }

    protected <X> void checkForRoleAnnotations(ProcessAnnotatedType<X> pat, Class<?> beanClass)
    {
        AnnotatedTypeBuilder<X> annotatedTypeBuilder =
            new AnnotatedTypeBuilder<X>().readFromType(pat.getAnnotatedType());

        boolean classLevelRoleInterceptorAdded = false;
        boolean patChanged = false;

        for (Annotation annotation : beanClass.getDeclaredAnnotations())
        {
            Class<?> annotationClass = annotation.annotationType();
            String annotationName = annotationClass.getName();

            if (annotationName.startsWith(EJB_PACKAGE_NAME))
            {
                return;
            }

            if (RunAs.class.equals(annotationClass))
            {
                annotatedTypeBuilder.addToClass(RunAsInterceptorBinding.INSTANCE);
                patChanged = true;
            }

            if (!classLevelRoleInterceptorAdded && roleAnnotationClasses.contains(annotationClass))
            {
                annotatedTypeBuilder.addToClass(RoleClassAdapterInterceptorBinding.INSTANCE);
                classLevelRoleInterceptorAdded = true;
                patChanged = true;
            }
        }

        if (!classLevelRoleInterceptorAdded)
        {
            //only add method-level interceptors (if needed) to improve the performance for the remaining methods
            for (Method method : beanClass.getDeclaredMethods())
            {
                if (!Modifier.isPublic(method.getModifiers()) ||
                    Modifier.isFinal(method.getModifiers()) ||
                    Modifier.isAbstract(method.getModifiers()) ||
                    Modifier.isStatic(method.getModifiers()))
                {
                    continue;
                }

                for (Annotation annotation : method.getDeclaredAnnotations())
                {
                    Class<?> annotationClass = annotation.annotationType();

                    if (roleAnnotationClasses.contains(annotationClass))
                    {
                        annotatedTypeBuilder.addToMethod(method, RoleMethodAdapterInterceptorBinding.INSTANCE);
                        patChanged = true;
                        break; //continue with the next method
                    }
                }
            }
        }

        if (patChanged)
        {
            pat.setAnnotatedType(annotatedTypeBuilder.create());
        }
    }
}
