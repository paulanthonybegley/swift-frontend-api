# Multi-Module CXF Architecture: Educational Guide

This guide explains the architectural design, module responsibilities, and implementation details of this "Source of Truth" Maven project.

## 1. Module Responsibilities

The project is divided into 6 modules to ensure a clean separation of concerns.

| Module | Responsibility | Why? |
| :--- | :--- | :--- |
| **root** | Parent POM | Centralizes dependency versions (BOMs) and plugin management to ensure consistency. |
| **api-spec** | Interface Contract | Uses `jaxrs-cxf` generator to create Java interfaces from `openapi.json`. It owns the "Contract". |
| **rest-model** | Data Transfer Objects | Generates POJOs used for serialization. Shared by both server and client. |
| **rest-mock** | Mock Implementation | Implements the generated interfaces. Includes SQLite persistence and Basic Auth. |
| **rest-service**| Client Library | Wraps CXF `JAXRSClientFactory`. Includes the **Logging Decorator**. |
| **job** | Orchestrator | The batch processor. Runs the integrated flow and applies the **Visitor Pattern**. |

---

## 2. Security & Persistence

### A. JAX-RS Filters for Security
We implement **Basic Authentication** using a `ContainerRequestFilter`. This is preferred over CXF Interceptors for standard JAX-RS portability.
- **Provider**: `BasicAuthFilter`
- **Mechanism**: Decodes `Authorization: Basic ...` header and validates against a temporary identity store.

### B. Dynamic Persistence (Factory Pattern)
The `rest-mock` server utilizes a `StorageFactory` to decide at runtime between **In-Memory (ConcurrentHashMap)** and **Persistent (SQLite)** storage.
- **SQLite Implementation**: Uses `spring-jdbc` (`JdbcTemplate`) for robust, boilerplate-free SQL interactions within a lightweight embedded database.
- **Benefit**: Allows the server to retain state across restarts, making it ideal for multi-stage transaction testing.

---

## 3. Observability & The Decorator Pattern

We employ a dual-layered logging strategy to provide 360-degree visibility.

### A. Wire-Level Logging (CXF Feature)
Enabled via `cxf-rt-features-logging`. It captures the raw HTTP request/response headers and payloads. Useful for debugging "What went over the wire".

### B. Method-Level Logging (Decorator Pattern)
We use the **Decorator Pattern** to wrap the `UetrProcessor` with a `LoggingUetrProcessor`.
- **Transparency**: The caller (Job module) doesn't know it's being logged.
- **Context**: Captures method parameters, return values, timing, and specific Java exceptions before they are converted to generic HTTP errors.

---

## 4. Advanced: The Visitor Pattern for Extensibility

To implement features like **Auditing**, **AsciiDoc**, and **PlantUML** without polluting generated models, we use the Visitor Pattern.

### Use Cases:
- **Auditing**: Traverses the graph to build a flattened business log.
- **AsciiDoc**: Generates technical manuals directly from mock data.
- **PlantUML**: Visualizes complex transaction routings as sequence diagrams.

---

## 5. Operational Excellence

### A. Executable "Fat" JARs
Both the `rest-mock` and `job` modules use the `maven-shade-plugin`.
- **Merging**: We use `AppendingTransformer` for `META-INF/cxf/bus-extensions.txt` to ensure CXF can find its HTTP transports even when shaded into a single JAR.

### B. Automated Orchestration (Makefile)
The `Makefile` serves as the entry point for local development.
- **`make run-all`**: A sophisticated target that backgrounds the server, waits for readiness, runs the processor, and cleans up. This ensures a consistent developer experience "out of the box".

---

## 6. Modern Testing Patterns
- **Property-Based Testing (jqwik)**: Defines properties that must hold true for *any* UETR or message structure.
- **JUnit 5 (Jupiter)**: The modern standard for test orchestration and Mockito integration.
