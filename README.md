# DeltaSpike EE-Role Bridge Add-on

A CDI portable extension that bridges Java EE / Jakarta EE security annotations
(`@RolesAllowed`, `@PermitAll`, `@DenyAll`, `@RunAs`) into the CDI interceptor
model. This allows pure CDI beans to use the same declarative role-based
security annotations that are traditionally limited to EJB components.

## Architecture

The extension (`RoleAdapterExtension`) observes `ProcessAnnotatedType` events
and programmatically adds interceptor bindings to beans that carry security
annotations. Three interceptors handle the runtime behaviour:

* **RoleClassAdapterInterceptor** -- evaluates class-level security annotations.
* **RoleMethodAdapterInterceptor** -- evaluates method-level security annotations
  when no class-level annotation is present.
* **RunAsInterceptor** -- manages the `@RunAs` role context via a thread-local
  stack (`RunAsRoleStorage`).

Role checks are delegated through the `RoleEvaluator` SPI, with a default
implementation that tries the servlet request first and falls back to the
EJB context.

## Requirements

* Java 25 or later
* Maven 3.6.3 or later

## Build

```bash
mvn clean verify
```

## Testing

Unit and CDI integration tests use JUnit 6 (Jupiter) and
[dynamic-cdi-test-bean-addon](https://github.com/os890/dynamic-cdi-test-bean-addon)
for lightweight CDI SE bootstrapping in tests.

```bash
mvn clean verify
```

## Quality Plugins

| Plugin          | Purpose                    |
|-----------------|----------------------------|
| Checkstyle      | Code-style enforcement     |
| Apache RAT      | License-header verification|
| JaCoCo          | Code-coverage reporting    |
| Enforcer        | Dependency & JDK checks    |
| Javadoc         | API documentation          |

## License

This project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
