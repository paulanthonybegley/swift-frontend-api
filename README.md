# CXF Payment Tracker System

A multi-module Maven project demonstrating JAX-RS services, state management, and job processing for payment transaction tracking.

## Modules

- **rest-model** - Generated domain models from OpenAPI spec
- **api-spec** - OpenAPI specification and generated JAX-RS interfaces
- **rest-mock** - Mock server implementation with SQLite persistence
- **rest-service** - Client service layer with local state tracking
- **job** - Scheduled job processor with visitor pattern

## Quick Start

```bash
# Build all modules
./mvnw clean install

# Run integrated flow (mock server + job processor)
make run-all

# Stop the mock server
make stop

# Clean all databases (for fresh start)
make clean-db
```

## Configuration

### Service-Side Storage
Control how the `rest-service` tracks UETR states:

```bash
# Use SQLite (default)
java -Dservice.storage=sqlite -jar job/target/job-1.0.0-SNAPSHOT.jar

# Use in-memory storage
java -Dservice.storage=memory -jar job/target/job-1.0.0-SNAPSHOT.jar
```

### Default UETR Seeding
When the service database has no active UETRs, it can automatically seed defaults:

```bash
# Enable seeding with 2 UETRs (default)
java -Dservice.seed.enabled=true -Dservice.seed.count=2 -jar job/target/job-1.0.0-SNAPSHOT.jar

# Disable automatic seeding
java -Dservice.seed.enabled=false -jar job/target/job-1.0.0-SNAPSHOT.jar

# Seed only 1 UETR
java -Dservice.seed.count=1 -jar job/target/job-1.0.0-SNAPSHOT.jar
```

### Mock Server Storage
Control the mock server's persistence:

```bash
# Use SQLite (default)
java -Dmock.storage=sqlite -jar rest-mock/target/rest-mock-1.0.0-SNAPSHOT.jar

# Use in-memory storage
java -Dmock.storage=memory -jar rest-mock/target/rest-mock-1.0.0-SNAPSHOT.jar
```

## Architecture

### Dual Database Design
- **`uetrs.db`** (Mock Server) - Source of truth for transaction lifecycle states
- **`service_uetrs.db`** (Service Layer) - Local cache of observed transaction states

This separation allows:
- **Resilience**: Service remembers progress even if mock is restarted
- **Performance**: Discovery queries local database instead of network
- **Independence**: Service and mock can be tested/deployed separately

### Job Scheduling
The job processor runs continuously, checking for active UETRs every 60 seconds. Transactions with `ACCC` status are automatically filtered from the work queue.

## Testing

```bash
# Run all tests
./mvnw test

# Run specific module tests
./mvnw test -pl rest-service
```

## Features

- ✅ JAX-RS REST services with CXF
- ✅ OpenAPI code generation
- ✅ SQLite & in-memory persistence
- ✅ Basic authentication
- ✅ Property-based testing with jqwik
- ✅ Visitor pattern for transaction processing
- ✅ Decorator pattern for logging
- ✅ Configurable job scheduling
- ✅ Automatic UETR seeding
