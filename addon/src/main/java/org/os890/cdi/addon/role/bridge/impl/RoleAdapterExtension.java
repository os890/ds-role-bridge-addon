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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RoleClassAdapterInterceptorBinding;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RoleMethodAdapterInterceptorBinding;
import org.os890.cdi.addon.role.bridge.impl.interceptor.RunAsInterceptorBinding;

import jakarta.annotation.security.RunAs;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * CDI portable extension that detects {@code @RolesAllowed}, {@code @PermitAll},
 * {@code @DenyAll} and {@code @RunAs} annotations and programmatically adds the
 * corresponding interceptor bindings so the role-bridge interceptors are activated.
 */
public class RoleAdapterExtension implements Extension, Deactivatable
{
    private static final String EJB_PACKAGE_NAME = "jakarta.ejb.";

    private static Set<Class<?>> roleAnnotationClasses = new HashSet<Class<?>>();

    static
    {
        roleAnnotationClasses.add(jakarta.annotation.security.DenyAll.class);
        roleAnnotationClasses.add(jakarta.annotation.security.PermitAll.class);
        roleAnnotationClasses.add(jakarta.annotation.security.RolesAllowed.class);
    }

    private Boolean isActivated = true;

    /**
     * Initialises the extension by checking whether it has been deactivated
     * via DeltaSpike class-deactivation configuration.
     *
     * @param beforeBeanDiscovery the CDI lifecycle event
     */
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    /**
     * Observer for every annotated type discovered by the container.
     * Delegates to {@link #checkForRoleAnnotations} when the extension is active.
     *
     * @param pat the process-annotated-type event
     * @param <X> the bean type
     */
    public <X> void checkForAuthenticationAnnotations(@Observes ProcessAnnotatedType<X> pat)
    {
        if (!isActivated)
        {
            return;
        }

        Class<?> beanClass = pat.getAnnotatedType().getJavaClass();

        checkForRoleAnnotations(pat, beanClass);
    }

    /**
     * Inspects the given bean class for security annotations and adds the
     * appropriate interceptor bindings via the CDI 4.x configurator API.
     *
     * @param pat       the process-annotated-type event
     * @param beanClass the Java class of the annotated type
     * @param <X>       the bean type
     */
    protected <X> void checkForRoleAnnotations(ProcessAnnotatedType<X> pat, Class<?> beanClass)
    {
        boolean classLevelRoleInterceptorAdded = false;
        boolean isEjb = isEjbClass(beanClass);

        for (Annotation annotation : beanClass.getDeclaredAnnotations())
        {
            Class<?> annotationClass = annotation.annotationType();

            if (isEjb)
            {
                if (RunAs.class.equals(annotationClass))
                {
                    pat.configureAnnotatedType().add(RunAsInterceptorBinding.INSTANCE);
                    return; //in case of EJBs we just check for @RunAs to propagate the info to the CDI context
                }
                continue;
            }

            if (RunAs.class.equals(annotationClass))
            {
                pat.configureAnnotatedType().add(RunAsInterceptorBinding.INSTANCE);
            }

            if (!classLevelRoleInterceptorAdded && roleAnnotationClasses.contains(annotationClass))
            {
                pat.configureAnnotatedType().add(RoleClassAdapterInterceptorBinding.INSTANCE);
                classLevelRoleInterceptorAdded = true;
            }
        }

        if (isEjb)
        {
            return; //we just need @RunWith from EJBs which is limited to the class-level
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
                        final Method targetMethod = method;
                        pat.configureAnnotatedType().methods().stream()
                            .filter(m -> m.getAnnotated().getJavaMember().equals(targetMethod))
                            .findFirst()
                            .ifPresent(m -> m.add(RoleMethodAdapterInterceptorBinding.INSTANCE));
                        break; //continue with the next method
                    }
                }
            }
        }
    }

    /**
     * Checks whether the given class is an EJB by looking for annotations
     * from the {@code jakarta.ejb} package.
     *
     * @param beanClass the class to inspect
     * @return {@code true} if the class carries an EJB annotation
     */
    private boolean isEjbClass(Class<?> beanClass)
    {
        for (Annotation annotation : beanClass.getDeclaredAnnotations())
        {
            Class<?> annotationClass = annotation.annotationType();
            String annotationName = annotationClass.getName();

            if (annotationName.startsWith(EJB_PACKAGE_NAME))
            {
                return true;
            }
        }
        return false;
    }
}
