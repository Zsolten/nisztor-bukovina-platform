# Project Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the existing prototype with a clean, testable monorepo foundation that contains no business functionality and is ready for requirement-linked feature pull requests.

**Architecture:** The backend is one Spring Boot modular monolith organized by business capability and protected by ArchUnit tests. The frontend is one React SPA with language-prefixed routing and an i18n provider. PostgreSQL is the only local infrastructure service in this PR; all persistent schema changes will begin with the first business feature.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Gradle 9.5.1, PostgreSQL 16, Flyway, Testcontainers, ArchUnit 1.4.2, React 19.2.6, TypeScript 6.0.2, Vite 8.0.12, Node.js 24.18.0, npm 11.16.0, Vitest 4.1.10, GitHub Actions.

## Global Constraints

- `docs/Requirements.pdf` is the normative requirements source.
- `docs/Product Vision and Goals.pdf` is the normative product-goal source.
- This PR must contain no guesthouse, room type, pricing, booking, administration, notification, activity, star tour, itinerary, or AI business functionality.
- The backend base package is exactly `com.bukovina.platform`.
- The backend is one Gradle project, not a Gradle multi-project build.
- Only PostgreSQL is included in Docker Compose; Redis and RabbitMQ remain out of scope.
- Spring Security is present but all HTTP requests are explicitly permitted in this foundation.
- Backend and frontend dependencies are pinned; no `latest`, dynamic version, or wildcard dependency is allowed.
- Code, packages, API identifiers, branches, and commits use English; developer and architecture documentation use Hungarian.
- Every task ends with an independently passing verification cycle and a focused Conventional Commit.
- Preserve unrelated user changes. Only delete the legacy paths explicitly named by this plan.

---

## Target File Map

### Backend

```text
backend/
├── build.gradle
├── settings.gradle
├── config/checkstyle/checkstyle.xml
├── gradle/wrapper/*
├── gradlew
├── gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/bukovina/platform/
    │   │   ├── BukovinaPlatformApplication.java
    │   │   ├── accommodation/**/package-info.java
    │   │   ├── tourism/**/package-info.java
    │   │   ├── support/**/package-info.java
    │   │   └── shared/**/package-info.java
    │   └── resources/application.yaml
    └── test/java/com/bukovina/platform/
        ├── BukovinaPlatformApplicationTests.java
        ├── architecture/ModuleArchitectureTests.java
        ├── architecture/ModulePackageStructureTests.java
        ├── testsupport/PostgreSqlTestContainerConfiguration.java
        └── shared/configuration/SecurityConfigurationTests.java
```

### Frontend

```text
frontend/
├── package.json
├── package-lock.json
├── index.html
├── eslint.config.js
├── .prettierrc.json
├── .prettierignore
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── vite.config.ts
└── src/
    ├── app/App.tsx
    ├── app/FoundationScreen.tsx
    ├── app/providers.tsx
    ├── app/router.tsx
    ├── app/App.test.tsx
    ├── app/router.test.tsx
    ├── i18n/config.ts
    ├── i18n/i18next.d.ts
    ├── i18n/languages.ts
    ├── i18n/resources.ts
    ├── test/setup.ts
    ├── main.tsx
    └── styles.css
```

### Repository and Documentation

```text
.github/workflows/ci.yml
.github/pull_request_template.md
.env.example
.gitignore
compose.yaml
README.md
docs/README.md
docs/architecture/*.md
docs/decisions/*.md
docs/api/openapi.yaml
docs/traceability/requirements-traceability.md
```

---

### Task 1: Replace the Legacy Backend with a PostgreSQL-Tested Spring Boot Base

**Files:**

- Delete: `backend/src/main/java/edu/`
- Delete: `backend/src/main/resources/application.properties`
- Delete: `backend/src/test/java/edu/`
- Delete: generated `backend/.gradle/` and `backend/build/` directories after resolving and verifying both paths are inside `backend/`
- Delete: `backend/.gitattributes`
- Delete: `backend/.gitignore`
- Delete: `backend/HELP.md`
- Create: `.gitignore`
- Modify: `backend/build.gradle`
- Modify: `backend/settings.gradle`
- Create: `backend/config/checkstyle/checkstyle.xml`
- Create: `backend/src/test/java/com/bukovina/platform/testsupport/PostgreSqlTestContainerConfiguration.java`
- Create: `backend/src/test/java/com/bukovina/platform/BukovinaPlatformApplicationTests.java`
- Create: `backend/src/main/java/com/bukovina/platform/BukovinaPlatformApplication.java`
- Create: `backend/src/main/resources/application.yaml`

**Interfaces:**

- Consumes: Gradle Wrapper 9.5.1, Docker daemon for Testcontainers.
- Produces: `com.bukovina.platform.BukovinaPlatformApplication`, a working Spring context, datasource and Flyway configuration, and the `./gradlew check` quality gate used by every later task.

- [ ] **Step 1: Replace the build definition and remove only the approved legacy backend files**

Create the root `.gitignore` before the first build so Gradle and npm output cannot enter a commit:

```gitignore
.env
.idea/
.vscode/
backend/.gradle/
backend/build/
frontend/node_modules/
frontend/dist/
*.log
```

Use this Gradle configuration:

```groovy
plugins {
    id 'java'
    id 'checkstyle'
    id 'org.springframework.boot' version '4.0.6'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'com.diffplug.spotless' version '8.9.0'
}

group = 'com.bukovina'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'

    runtimeOnly 'org.flywaydb:flyway-database-postgresql'
    runtimeOnly 'org.postgresql:postgresql'

    testImplementation 'org.springframework.boot:spring-boot-starter-actuator-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-validation-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:testcontainers-junit-jupiter'
    testImplementation 'org.testcontainers:testcontainers-postgresql'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.4.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

checkstyle {
    toolVersion = '13.9.0'
}

spotless {
    java {
        googleJavaFormat('1.35.0')
        target 'src/**/*.java'
    }
    groovyGradle {
        target '*.gradle'
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

Set `backend/settings.gradle` to:

```groovy
rootProject.name = 'bukovina-platform-backend'
```

Use a minimal Checkstyle configuration that enforces imports, braces and Java identifier naming while leaving formatting to Spotless:

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
        "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="fileExtensions" value="java"/>
    <module name="TreeWalker">
        <module name="AvoidStarImport"/>
        <module name="UnusedImports"/>
        <module name="NeedBraces"/>
        <module name="OneStatementPerLine"/>
        <module name="UpperEll"/>
        <module name="TypeName"/>
        <module name="MethodName"/>
        <module name="MemberName"/>
        <module name="ParameterName"/>
        <module name="LocalVariableName"/>
    </module>
</module>
```

- [ ] **Step 2: Write the failing PostgreSQL context test and reusable container configuration**

Create the test-only PostgreSQL configuration:

```java
package com.bukovina.platform.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSqlTestContainerConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSqlContainer() {
        return new PostgreSQLContainer("postgres:16-alpine");
    }
}
```

Then create the context test:

```java
package com.bukovina.platform;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@Import(PostgreSqlTestContainerConfiguration.class)
class BukovinaPlatformApplicationTests {

    @Test
    void contextLoads() {}
}
```

- [ ] **Step 3: Run the test and verify the expected failure**

Run from `backend`:

```powershell
.\gradlew.bat test --tests com.bukovina.platform.BukovinaPlatformApplicationTests
```

Expected: FAIL because `BukovinaPlatformApplication` and the new application configuration do not exist yet. If the failure is instead a Docker connectivity error, start Docker Desktop and rerun before implementing.

- [ ] **Step 4: Implement the minimal application and configuration**

```java
package com.bukovina.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BukovinaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(BukovinaPlatformApplication.class, args);
    }
}
```

Create `application.yaml`:

```yaml
spring:
  application:
    name: bukovina-platform
  config:
    import: optional:file:../.env[.properties]
  datasource:
    url: ${DB_URL:jdbc:postgresql://127.0.0.1:15432/bukovina_platform}
    username: ${DB_USERNAME:bukovina_app}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

server:
  port: ${SERVER_PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health
```

- [ ] **Step 5: Format and run the backend test suite**

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat check
```

Expected: PASS. The context starts against `postgres:16-alpine`, Flyway reports no migrations, and Checkstyle/Spotless are green.

- [ ] **Step 6: Commit the backend base**

```powershell
git add .gitignore backend
git commit -m "chore(backend): rebuild Spring Boot foundation"
```

---

### Task 2: Establish Package-Based Modules and ArchUnit Boundaries

**Files:**

- Create: `backend/src/main/java/com/bukovina/platform/accommodation/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/accommodation/roomtype/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/accommodation/pricing/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/accommodation/amenity/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/accommodation/booking/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/tourism/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/tourism/activity/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/tourism/startour/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/tourism/itinerary/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/support/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/support/authentication/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/support/administration/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/support/translation/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/support/notification/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/shared/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/shared/configuration/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/shared/exception/package-info.java`
- Create: `backend/src/main/java/com/bukovina/platform/shared/validation/package-info.java`
- Create: `backend/src/test/java/com/bukovina/platform/architecture/ModuleArchitectureTests.java`
- Create: `backend/src/test/java/com/bukovina/platform/architecture/ModulePackageStructureTests.java`

**Interfaces:**

- Consumes: The root package and `./gradlew check` from Task 1.
- Produces: Named business/support packages and executable dependency rules used by all future modules.

- [ ] **Step 1: Write the failing package-structure test before module packages exist**

This source-layout test gives the empty foundation modules an explicit red-green cycle without relying on whether the Java compiler emits bytecode for an unannotated `package-info.java` file:

```java
package com.bukovina.platform.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ModulePackageStructureTests {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @ParameterizedTest(name = "{0} is documented")
    @MethodSource("modulePackages")
    void packageHasDocumentation(String packageName) {
        Path packageInfo = SOURCE_ROOT.resolve(packageName.replace('.', '/')).resolve("package-info.java");

        assertTrue(Files.isRegularFile(packageInfo), () -> "Missing " + packageInfo);
    }

    static Stream<String> modulePackages() {
        return Stream.of(
                "com.bukovina.platform.accommodation",
                "com.bukovina.platform.accommodation.guesthouse",
                "com.bukovina.platform.accommodation.roomtype",
                "com.bukovina.platform.accommodation.pricing",
                "com.bukovina.platform.accommodation.amenity",
                "com.bukovina.platform.accommodation.booking",
                "com.bukovina.platform.tourism",
                "com.bukovina.platform.tourism.activity",
                "com.bukovina.platform.tourism.startour",
                "com.bukovina.platform.tourism.itinerary",
                "com.bukovina.platform.support",
                "com.bukovina.platform.support.authentication",
                "com.bukovina.platform.support.administration",
                "com.bukovina.platform.support.translation",
                "com.bukovina.platform.support.notification",
                "com.bukovina.platform.shared",
                "com.bukovina.platform.shared.configuration",
                "com.bukovina.platform.shared.exception",
                "com.bukovina.platform.shared.validation");
    }
}
```

- [ ] **Step 2: Run the structure test and verify the expected failure**

```powershell
.\gradlew.bat test --tests com.bukovina.platform.architecture.ModulePackageStructureTests
```

Expected: FAIL with one missing `package-info.java` assertion per planned package.

- [ ] **Step 3: Add documented package-info classes**

Use the following exact pattern for every package, changing only the description and package declaration:

```java
/** Owns guesthouse presentation and content management. */
package com.bukovina.platform.accommodation.guesthouse;
```

Descriptions and declarations:

```text
Accommodation business area.
package com.bukovina.platform.accommodation;

Owns guesthouse presentation and content management.
package com.bukovina.platform.accommodation.guesthouse;

Owns room type definitions and capacity rules.
package com.bukovina.platform.accommodation.roomtype;

Owns price policies and price calculations.
package com.bukovina.platform.accommodation.pricing;

Owns accommodation amenities and their seasonal terms.
package com.bukovina.platform.accommodation.amenity;

Owns booking request use cases and state transitions.
package com.bukovina.platform.accommodation.booking;

Tourism content and itinerary business area.
package com.bukovina.platform.tourism;

Owns attractions and reusable activities.
package com.bukovina.platform.tourism.activity;

Owns star tours, ordered stops and tags.
package com.bukovina.platform.tourism.startour;

Owns later itinerary-planning use cases.
package com.bukovina.platform.tourism.itinerary;

Supporting application capabilities.
package com.bukovina.platform.support;

Owns administrator authentication.
package com.bukovina.platform.support.authentication;

Coordinates administrative use cases exposed by business modules.
package com.bukovina.platform.support.administration;

Owns supported languages and translation fallback policy.
package com.bukovina.platform.support.translation;

Owns email notification delivery.
package com.bukovina.platform.support.notification;

Shared technical building blocks without business rules.
package com.bukovina.platform.shared;

Shared application configuration.
package com.bukovina.platform.shared.configuration;

Shared technical exception handling.
package com.bukovina.platform.shared.exception;

Shared reusable validation infrastructure.
package com.bukovina.platform.shared.validation;
```

- [ ] **Step 4: Add ArchUnit dependency rules**

```java
package com.bukovina.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.bukovina.platform")
class ModuleArchitectureTests {

    @ArchTest
    static final ArchRule SHARED_MUST_NOT_DEPEND_ON_BUSINESS_AREAS =
            noClasses()
                    .that()
                    .resideInAPackage("..shared..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..accommodation..", "..tourism..", "..support..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule ACCOMMODATION_MUST_NOT_DEPEND_ON_TOURISM =
            noClasses()
                    .that()
                    .resideInAPackage("..accommodation..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..tourism..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule TOURISM_MUST_NOT_DEPEND_ON_ACCOMMODATION =
            noClasses()
                    .that()
                    .resideInAPackage("..tourism..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..accommodation..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule TOP_LEVEL_PACKAGES_MUST_BE_FREE_OF_CYCLES =
            slices()
                    .matching("com.bukovina.platform.(*)..")
                    .should()
                    .beFreeOfCycles();
}
```

- [ ] **Step 5: Run structure, architecture and full backend checks**

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat test --tests com.bukovina.platform.architecture.ModulePackageStructureTests
.\gradlew.bat test --tests com.bukovina.platform.architecture.ModuleArchitectureTests
.\gradlew.bat check
```

Expected: PASS. No `controller`, `service`, `dao`, `model` or `dto` package exists yet.

- [ ] **Step 6: Commit module boundaries**

```powershell
git add backend/src/main/java/com/bukovina/platform backend/src/test/java/com/bukovina/platform/architecture
git commit -m "test(architecture): enforce modular package boundaries"
```

---

### Task 3: Configure an Explicitly Open Foundation Security Chain

**Files:**

- Create: `backend/src/test/java/com/bukovina/platform/shared/configuration/SecurityConfigurationTests.java`
- Create: `backend/src/main/java/com/bukovina/platform/shared/configuration/SecurityConfiguration.java`

**Interfaces:**

- Consumes: Spring Security and web test dependencies from Task 1.
- Produces: A temporary `SecurityFilterChain` that permits unauthenticated GET and POST requests while the authentication feature remains out of scope.

- [ ] **Step 1: Write failing GET, POST and health tests**

```java
package com.bukovina.platform.shared.configuration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import({
    PostgreSqlTestContainerConfiguration.class,
    SecurityConfigurationTests.TestEndpoint.class
})
class SecurityConfigurationTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void allowsUnauthenticatedGetRequests() throws Exception {
        mockMvc.perform(get("/__test/open")).andExpect(status().isOk());
    }

    @Test
    void allowsUnauthenticatedPostRequests() throws Exception {
        mockMvc.perform(post("/__test/open")).andExpect(status().isOk());
    }

    @Test
    void exposesOnlyAHealthyActuatorFoundation() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @RestController
    static class TestEndpoint {

        @GetMapping("/__test/open")
        String get() {
            return "open";
        }

        @PostMapping("/__test/open")
        String post() {
            return "open";
        }
    }
}
```

The imported test configuration starts the same real PostgreSQL 16 container pattern used by the application context test. It remains under `src/test` and cannot enter production code.

- [ ] **Step 2: Run the security tests and verify the expected failure**

```powershell
.\gradlew.bat test --tests com.bukovina.platform.shared.configuration.SecurityConfigurationTests
```

Expected: FAIL. The test-only GET endpoint is unauthorized and the POST endpoint is rejected by CSRF under default Spring Security behavior.

- [ ] **Step 3: Implement the temporary permit-all security chain**

```java
package com.bukovina.platform.shared.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
```

- [ ] **Step 4: Run security and full backend checks**

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat test --tests com.bukovina.platform.shared.configuration.SecurityConfigurationTests
.\gradlew.bat check
```

Expected: PASS. GET and POST are open, `/actuator/health` is `UP`, and `/actuator/env` is not exposed.

- [ ] **Step 5: Commit the temporary security foundation**

```powershell
git add backend/src/main/java/com/bukovina/platform/shared/configuration backend/src/test/java/com/bukovina/platform/shared/configuration
git commit -m "chore(security): permit requests in foundation phase"
```

---

### Task 4: Add Reproducible Local PostgreSQL Configuration

**Files:**

- Create: `.env.example`
- Create: `compose.yaml`

**Interfaces:**

- Consumes: The datasource property names from Task 1.
- Produces: A local PostgreSQL 16 service on host port `15432` and a documented `.env` contract used by Docker Compose and Spring Boot.

- [ ] **Step 1: Add a failing Compose validation command before the files exist**

Run from the repository root:

```powershell
$env:POSTGRES_PASSWORD = 'foundation-validation-password'
docker compose config
```

Expected: FAIL because `compose.yaml` does not exist.

- [ ] **Step 2: Create the environment contract**

Create `.env.example`:

```properties
SERVER_PORT=8080
POSTGRES_DB=bukovina_platform
POSTGRES_USER=bukovina_app
POSTGRES_PASSWORD=
POSTGRES_HOST_PORT=15432
DB_URL=jdbc:postgresql://127.0.0.1:15432/bukovina_platform
DB_USERNAME=bukovina_app
DB_PASSWORD=
```

- [ ] **Step 3: Create PostgreSQL-only Docker Compose**

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: bukovina-platform-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-bukovina_platform}
      POSTGRES_USER: ${POSTGRES_USER:-bukovina_app}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD in .env}
    ports:
      - "${POSTGRES_HOST_PORT:-15432}:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10

volumes:
  postgres-data:
```

- [ ] **Step 4: Validate configuration and container health**

Run in one PowerShell session:

```powershell
$env:POSTGRES_PASSWORD = 'foundation-validation-password'
docker compose config
docker compose up -d postgres
docker compose ps
```

Expected: `docker compose config` succeeds and `bukovina-platform-postgres` reaches `healthy`. Do not use `docker compose down -v`; the named development volume must not be deleted by verification.

- [ ] **Step 5: Verify the backend against the Compose database**

Run from `backend` in a PowerShell session where the same temporary password is set:

```powershell
$env:DB_PASSWORD = 'foundation-validation-password'
.\gradlew.bat bootRun
```

In another terminal:

```powershell
curl.exe --fail http://localhost:8080/actuator/health
```

Expected response includes `"status":"UP"`. Stop `bootRun`, then run `docker compose down` from the repository root.

- [ ] **Step 6: Commit local infrastructure**

```powershell
git add .env.example compose.yaml
git commit -m "chore(infrastructure): add local PostgreSQL environment"
```

---

### Task 5: Replace the Legacy Frontend with a Tested React Foundation

**Files:**

- Delete: root `package-lock.json` left by the prototype; npm ownership is under `frontend/`
- Delete: all existing files under `frontend/`
- Create: `frontend/package.json`
- Generate: `frontend/package-lock.json`
- Create: `frontend/index.html`
- Create: `frontend/eslint.config.js`
- Create: `frontend/.prettierrc.json`
- Create: `frontend/.prettierignore`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.app.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/src/test/setup.ts`
- Create: `frontend/src/app/App.test.tsx`
- Create: `frontend/src/app/FoundationScreen.tsx`
- Create: `frontend/src/app/App.tsx`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/styles.css`

**Interfaces:**

- Consumes: Node.js 24.18.0 and npm 11.16.0.
- Produces: A minimal React application, stable npm scripts and a locked dependency graph used by Task 6 and CI.

- [ ] **Step 1: Remove the legacy frontend and create the pinned package manifest**

```json
{
  "name": "bukovina-platform-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "engines": {
    "node": "24.x",
    "npm": "11.x"
  },
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "lint": "eslint .",
    "test": "vitest run",
    "test:watch": "vitest",
    "format": "prettier --write .",
    "format:check": "prettier --check ."
  },
  "dependencies": {
    "i18next": "26.3.6",
    "react": "19.2.6",
    "react-dom": "19.2.6",
    "react-i18next": "17.0.11",
    "react-router-dom": "7.18.2"
  },
  "devDependencies": {
    "@eslint/js": "10.0.1",
    "@testing-library/dom": "10.4.1",
    "@testing-library/jest-dom": "7.0.0",
    "@testing-library/react": "16.3.2",
    "@types/node": "24.13.3",
    "@types/react": "19.2.18",
    "@types/react-dom": "19.2.4",
    "@vitejs/plugin-react": "6.0.5",
    "eslint": "10.8.0",
    "eslint-plugin-react-hooks": "7.1.1",
    "eslint-plugin-react-refresh": "0.5.3",
    "globals": "17.9.0",
    "jsdom": "30.0.1",
    "prettier": "3.9.6",
    "typescript": "6.0.2",
    "typescript-eslint": "8.66.0",
    "vite": "8.0.12",
    "vitest": "4.1.10"
  }
}
```

Run from `frontend`:

```powershell
npm.cmd install
```

Expected: `package-lock.json` is generated with no peer dependency errors.

- [ ] **Step 2: Add TypeScript, Vite, ESLint, Prettier and test configuration**

Create `tsconfig.json`:

```json
{
  "files": [],
  "references": [{ "path": "./tsconfig.app.json" }, { "path": "./tsconfig.node.json" }]
}
```

Create `tsconfig.app.json`:

```json
{
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
    "target": "ES2023",
    "useDefineForClassFields": true,
    "lib": ["ES2023", "DOM"],
    "module": "ESNext",
    "types": ["vite/client"],
    "moduleResolution": "Bundler",
    "allowImportingTsExtensions": true,
    "verbatimModuleSyntax": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedSideEffectImports": true,
    "skipLibCheck": true
  },
  "include": ["src"]
}
```

Create `tsconfig.node.json`:

```json
{
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.node.tsbuildinfo",
    "target": "ES2023",
    "lib": ["ES2023"],
    "module": "NodeNext",
    "allowImportingTsExtensions": true,
    "verbatimModuleSyntax": true,
    "moduleDetection": "force",
    "noEmit": true,
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "skipLibCheck": true,
    "types": ["node"]
  },
  "include": ["vite.config.ts"]
}
```

Configure Vite with React, `jsdom`, test setup and the approved API proxy. Import `defineConfig` from `vitest/config` so the `test` property is type-safe:

```typescript
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
})
```

Create test setup:

```typescript
import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

afterEach(cleanup)
```

Create `eslint.config.js`:

```javascript
import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'

export default tseslint.config(
  { ignores: ['dist'] },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.flat.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
    },
  },
)
```

Create `.prettierrc.json`:

```json
{
  "singleQuote": true,
  "semi": false,
  "trailingComma": "all",
  "printWidth": 100
}
```

Create `.prettierignore`:

```text
dist
node_modules
package-lock.json
```

Create `index.html`:

```html
<!doctype html>
<html lang="hu">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Nisztor-Bukovina Platform</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 3: Write the failing smoke test**

```tsx
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import FoundationScreen from './FoundationScreen'

describe('FoundationScreen', () => {
  it('renders the foundation product name', () => {
    render(<FoundationScreen />)

    expect(
      screen.getByRole('heading', { name: 'Nisztor-Bukovina Platform' }),
    ).toBeInTheDocument()
  })
})
```

- [ ] **Step 4: Run the smoke test and verify the expected failure**

```powershell
npm.cmd run test
```

Expected: FAIL because `src/app/FoundationScreen.tsx` does not exist.

- [ ] **Step 5: Implement the minimal smoke screen**

```tsx
interface FoundationScreenProps {
  title?: string
}

function FoundationScreen({ title = 'Nisztor-Bukovina Platform' }: FoundationScreenProps) {
  return (
    <main>
      <h1>{title}</h1>
    </main>
  )
}

export default FoundationScreen
```

Create `App.tsx`:

```tsx
import FoundationScreen from './FoundationScreen'

function App() {
  return <FoundationScreen />
}

export default App
```

Create `main.tsx`:

```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './app/App'
import './styles.css'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('Root element was not found')
}

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

Create `styles.css`:

```css
:root {
  font-family: Arial, Helvetica, sans-serif;
  color: #1f2933;
  background: #ffffff;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
}

main {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 2rem;
  text-align: center;
}
```

- [ ] **Step 6: Run all frontend quality gates**

```powershell
npm.cmd run format
npm.cmd run format:check
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

Expected: all commands PASS and `frontend/dist` is generated but ignored by Git.

- [ ] **Step 7: Commit the clean frontend base**

```powershell
git add frontend package-lock.json
git commit -m "chore(frontend): rebuild React foundation"
```

---

### Task 6: Add Tested Language-Prefixed Routing and i18n Foundation

**Files:**

- Create: `frontend/src/i18n/languages.ts`
- Create: `frontend/src/i18n/resources.ts`
- Create: `frontend/src/i18n/i18next.d.ts`
- Create: `frontend/src/i18n/config.ts`
- Create: `frontend/src/app/providers.tsx`
- Create: `frontend/src/app/router.tsx`
- Create: `frontend/src/app/router.test.tsx`
- Modify: `frontend/src/app/App.tsx`

**Interfaces:**

- Consumes: React test base and pinned router/i18n dependencies from Task 5.
- Produces: `SUPPORTED_LANGUAGES`, `DEFAULT_LANGUAGE`, `PREFERRED_LANGUAGE_KEY`, `readPreferredLanguage()`, `appRoutes`, and the language routing behavior required by all later pages.

- [ ] **Step 1: Write routing tests before the router exists**

Tests must cover all four approved behaviors:

```tsx
import { render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { AppProviders } from './providers'
import { appRoutes } from './router'

describe('language routing', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('redirects the root path to Hungarian by default', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
    expect(
      await screen.findByRole('heading', { name: 'Nisztor-Bukovina Platform' }),
    ).toBeVisible()
  })

  it('redirects the root path to the remembered supported language', async () => {
    localStorage.setItem('preferredLanguage', 'ro')
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/ro'))
  })

  it('redirects an unsupported language to Hungarian', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/de'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
  })

  it('stores a supported language when its route is opened', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/en'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(localStorage.getItem('preferredLanguage')).toBe('en'))
  })
})
```

- [ ] **Step 2: Run the routing tests and verify the expected failure**

```powershell
npm.cmd run test
```

Expected: FAIL because `providers.tsx`, `router.tsx` and the i18n foundation do not exist.

- [ ] **Step 3: Implement language constants and safe storage parsing**

```typescript
export const SUPPORTED_LANGUAGES = ['hu', 'ro', 'en'] as const
export type Language = (typeof SUPPORTED_LANGUAGES)[number]

export const DEFAULT_LANGUAGE: Language = 'hu'
export const PREFERRED_LANGUAGE_KEY = 'preferredLanguage'

export function isSupportedLanguage(value: unknown): value is Language {
  return (
    typeof value === 'string' &&
    (SUPPORTED_LANGUAGES as readonly string[]).includes(value)
  )
}

export function readPreferredLanguage(): Language {
  const storedLanguage = localStorage.getItem(PREFERRED_LANGUAGE_KEY)
  return isSupportedLanguage(storedLanguage) ? storedLanguage : DEFAULT_LANGUAGE
}
```

Create `resources.ts`:

```typescript
export const resources = {
  hu: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
  ro: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
  en: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
} as const

export type TranslationResources = (typeof resources)['hu']['translation']
```

Create `i18next.d.ts` so i18next 26 keeps the readable string-key API and checks keys against the Hungarian source bundle:

```typescript
import type { TranslationResources } from './resources'

declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: 'translation'
    resources: TranslationResources
    enableSelector: false
  }
}
```

Create `config.ts`:

```typescript
import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import { DEFAULT_LANGUAGE, SUPPORTED_LANGUAGES } from './languages'
import { resources } from './resources'

void i18n.use(initReactI18next).init({
  resources,
  lng: DEFAULT_LANGUAGE,
  fallbackLng: DEFAULT_LANGUAGE,
  defaultNS: 'translation',
  supportedLngs: SUPPORTED_LANGUAGES,
  initAsync: false,
  interpolation: { escapeValue: false },
})

export default i18n
```

- [ ] **Step 4: Implement providers and route definitions**

Create `providers.tsx`:

```tsx
import type { ReactNode } from 'react'
import { I18nextProvider } from 'react-i18next'
import i18n from '../i18n/config'

interface AppProvidersProps {
  children: ReactNode
}

export function AppProviders({ children }: AppProvidersProps) {
  return <I18nextProvider i18n={i18n}>{children}</I18nextProvider>
}
```

Create `router.tsx`:

```tsx
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Navigate,
  redirect,
  type RouteObject,
  useParams,
} from 'react-router-dom'
import {
  DEFAULT_LANGUAGE,
  isSupportedLanguage,
  PREFERRED_LANGUAGE_KEY,
  readPreferredLanguage,
} from '../i18n/languages'
import FoundationScreen from './FoundationScreen'

function LanguageRoute() {
  const { lang } = useParams()
  const { i18n, t } = useTranslation()

  useEffect(() => {
    if (!isSupportedLanguage(lang)) {
      return
    }

    localStorage.setItem(PREFERRED_LANGUAGE_KEY, lang)
    void i18n.changeLanguage(lang)
  }, [i18n, lang])

  if (!isSupportedLanguage(lang)) {
    return <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />
  }

  return <FoundationScreen title={t('app.title')} />
}

export const appRoutes: RouteObject[] = [
  {
    path: '/',
    loader: () => redirect(`/${readPreferredLanguage()}`),
  },
  {
    path: '/:lang',
    element: <LanguageRoute />,
  },
  {
    path: '*',
    element: <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />,
  },
]
```

The route table has these semantics:

```text
/       loader redirect to /{readPreferredLanguage()}
/:lang  LanguageRoute
*       redirect to /hu
```

Replace `App.tsx` with:

```tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { AppProviders } from './providers'
import { appRoutes } from './router'

const router = createBrowserRouter(appRoutes)

function App() {
  return (
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  )
}

export default App
```

- [ ] **Step 5: Run routing and full frontend checks**

```powershell
npm.cmd run format
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

Expected: PASS. The four language-routing tests and original smoke test remain green.

- [ ] **Step 6: Commit routing and i18n foundation**

```powershell
git add frontend/src
git commit -m "feat(frontend): add language routing foundation"
```

---

### Task 7: Restore Specification-Driven Architecture Documentation

**Files:**

- Add: `docs/Requirements.pdf`
- Add: `docs/Product Vision and Goals.pdf`
- Create: `docs/README.md`
- Create: `docs/architecture/system-architecture.md`
- Create: `docs/architecture/backend-architecture.md`
- Create: `docs/architecture/frontend-architecture.md`
- Create: `docs/architecture/data-model.md`
- Create: `docs/architecture/infrastructure.md`
- Create: `docs/architecture/security.md`
- Create: `docs/decisions/ADR-001-modular-monolith.md`
- Create: `docs/decisions/ADR-002-business-module-boundaries.md`
- Create: `docs/decisions/ADR-003-translation-tables.md`
- Create: `docs/decisions/ADR-004-room-type-based-booking.md`
- Create: `docs/decisions/ADR-005-redis-rate-limiting.md`
- Create: `docs/decisions/ADR-006-rabbitmq-notifications.md`
- Create: `docs/api/openapi.yaml`
- Create: `docs/traceability/requirements-traceability.md`

**Interfaces:**

- Consumes: The approved design document and the two normative PDFs.
- Produces: Version-controlled technical architecture, decision history, API contract base and traceability rules for all later PRs.

- [ ] **Step 1: Create the documentation index and authority rule**

`docs/README.md` must state this exact precedence:

```text
1. A Requirements.pdf a normatív követelményforrás.
2. A Product Vision and Goals.pdf a normatív termékcél-forrás.
3. Az architektúradokumentumok leírják a követelmények technikai megvalósítását.
4. Az ADR-ek indokolják a jelentős technikai döntéseket.
5. Az openapi.yaml a frontend és a backend API-szerződése.
6. Alacsonyabb szintű dokumentum egyik normatív PDF-et sem írhatja felül.
```

It must also require every feature ticket and PR to reference at least one `FR-*` or `NFR-*` identifier.

- [ ] **Step 2: Restore architecture documents from the approved design**

Required sections:

```text
system-architecture.md
- two business areas: accommodation and tourism
- support capabilities
- system boundaries
- dependency principles

backend-architecture.md
- package-by-feature structure
- controller -> service -> dao layering
- module dependency rules
- transaction and testing strategy

frontend-architecture.md
- feature structure
- language routing
- localStorage constraints
- relative /api contract

data-model.md
- relational PostgreSQL principles
- per-entity translation tables
- RoomType rather than physical Room
- planned accommodation and tourism table groups
- no business tables in foundation

infrastructure.md
- PostgreSQL now
- Redis only for rate limiting
- RabbitMQ only for P2 notifications
- local/test/production boundaries

security.md
- guests are anonymous
- admin security is deferred
- current permit-all foundation is temporary
- validation, privacy and rate-limit requirements
```

- [ ] **Step 3: Restore six ADRs**

Each ADR must contain `Állapot`, `Kapcsolódó követelmények`, `Kontextus`, `Döntés`, and `Következmények`. Statuses are:

```text
ADR-001 modular monolith: accepted
ADR-002 two business areas: accepted
ADR-003 per-entity translation tables: accepted
ADR-004 room-type-based booking: accepted
ADR-005 Redis rate limiting: planned
ADR-006 RabbitMQ notifications: deferred to P2
```

- [ ] **Step 4: Add the empty contract-first OpenAPI base**

```yaml
openapi: 3.1.0
info:
  title: Nisztor-Bukovina Platform API
  version: 0.1.0
  description: Contract-first REST API for the Nisztor-Bukovina Platform.
servers:
  - url: /api
paths: {}
```

Do not add speculative business endpoints or an authentication scheme.

- [ ] **Step 5: Restore the traceability matrix**

Map each requirement group to its owner:

```text
FR-GH-*      accommodation.guesthouse
FR-ROOM-*    accommodation.roomtype
FR-PRICE-*   accommodation.pricing
FR-BOOK-*    accommodation.booking
FR-PAY-*     accommodation.booking
FR-SERVICE-* accommodation.amenity
FR-TOUR-*    tourism.startour + tourism.activity
FR-FAV-*     frontend tourism favorites
FR-ALT-*     tourism.startour, P2
FR-ADMIN-*   support.administration + support.authentication
FR-EMAIL-*   support.notification, P1
NFR-SEC-*    security infrastructure and affected modules
NFR-TEST-*   complete automated test suite
```

- [ ] **Step 6: Self-review all documents**

```powershell
rg -n "TBD|TODO|FIXME|latest|guesthouse_platform|accommodationservice" docs/README.md docs/architecture docs/decisions docs/api docs/traceability
git diff --check -- docs/README.md docs/architecture docs/decisions docs/api docs/traceability
```

Expected: no placeholder or legacy-name matches and no whitespace errors. References to future P1/P2 work are explicit scope statements, not placeholders.

- [ ] **Step 7: Commit normative and derived documentation**

```powershell
git add docs
git commit -m "docs(architecture): restore specification-driven documentation"
```

---

### Task 8: Add CI, PR Governance and the Root Developer Guide

**Files:**

- Create: `.github/workflows/ci.yml`
- Create: `.github/pull_request_template.md`
- Create: `README.md`

**Interfaces:**

- Consumes: Backend `check` task, frontend npm scripts, Docker Compose contract and documentation index from previous tasks.
- Produces: Required pull-request checks and the final onboarding path for every later feature.

- [ ] **Step 1: Add the GitHub Actions workflow**

```yaml
name: CI

on:
  pull_request:
  push:
    branches:
      - main

permissions:
  contents: read

jobs:
  backend-check:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle
          cache-dependency-path: |
            backend/*.gradle
            backend/gradle/wrapper/gradle-wrapper.properties
      - run: chmod +x gradlew
      - run: ./gradlew check --no-daemon

  frontend-check:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-node@v6
        with:
          node-version: '24.18.0'
          cache: npm
          cache-dependency-path: frontend/package-lock.json
      - run: npm ci
      - run: npm run format:check
      - run: npm run lint
      - run: npm run test
      - run: npm run build
```

- [ ] **Step 2: Add the PR template**

```markdown
## Requirement

- Requirement ID:

## Scope

## Acceptance Criteria

## Test Evidence

## Database Migration

- [ ] No database change
- [ ] Migration included and tested

## Security and Privacy Impact

## Screenshots

Not applicable when the PR has no UI change.

## Out of Scope
```

The foundation PR uses `N/A - technical foundation` as its requirement value. Later feature PRs must use actual `FR-*` or `NFR-*` identifiers.

- [ ] **Step 3: Write the root README**

The Hungarian README must contain exact prerequisites and commands:

```text
Előfeltételek
- Java 21
- Node.js 24.18.0 LTS
- npm 11.16.0
- Docker Desktop with Docker Compose

Első beállítás
- copy .env.example to .env
- set POSTGRES_PASSWORD and DB_PASSWORD to the same local value

Adatbázis
- docker compose up -d postgres
- docker compose ps

Backend Windows alatt
- cd backend
- .\gradlew.bat bootRun
- .\gradlew.bat check

Frontend Windows alatt
- cd frontend
- npm.cmd ci
- npm.cmd run dev
- npm.cmd run format:check
- npm.cmd run lint
- npm.cmd run test
- npm.cmd run build

Elérhetőségek
- frontend: http://localhost:5173
- backend health: http://localhost:8080/actuator/health
```

Also explain the two business areas, the no-business-functionality foundation scope, and the requirement-linked branch/PR convention.

- [ ] **Step 4: Run complete local verification from a clean dependency state**

Backend:

```powershell
cd backend
.\gradlew.bat clean check
```

Frontend:

```powershell
cd frontend
npm.cmd ci
npm.cmd run format:check
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

Repository and Compose:

```powershell
cd ..
$env:POSTGRES_PASSWORD = 'foundation-validation-password'
docker compose config
git diff --check
git diff --check origin/main...HEAD
git status --short
```

Expected:

- backend `BUILD SUCCESSFUL`;
- all frontend commands exit with code 0;
- Compose configuration is valid and includes only PostgreSQL;
- no whitespace errors;
- only intended foundation files are modified or added;
- no `edu.bbte.guesthouse_platform` source remains;
- no business entity, repository, controller or migration exists.

- [ ] **Step 5: Commit CI and developer workflow**

```powershell
git add .github README.md
git commit -m "ci: add foundation quality gates"
```

- [ ] **Step 6: Prepare the pull request evidence**

Record in the PR description:

```text
Requirement ID: N/A - technical foundation
Test evidence: ./gradlew clean check; npm ci; format:check; lint; test; build
Database migration: no business migration
Security impact: Spring Security present, temporary permit-all chain documented
Out of scope: all business functionality, Redis, RabbitMQ, authentication
```

Do not push or create the PR until the user explicitly requests publishing.

---

## Plan Self-Review Result

- Spec coverage: all twelve foundation acceptance criteria in the approved design map to Tasks 1 through 8.
- Scope: no business entity, business endpoint, seed data or speculative API operation is included.
- Type consistency: package, application, environment variable, route, storage key and command names are consistent across tasks.
- Placeholder scan: the plan contains no `TBD`, implementation `TODO`, dynamic dependency or unspecified version.
- Destructive boundary: only the explicitly approved legacy backend source roots and existing frontend directory are replaced; Gradle Wrapper and normative PDFs are preserved.
