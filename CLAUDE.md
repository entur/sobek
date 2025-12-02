# Sobek - Vehicle Register System

## Project Overview

**Sobek** is a comprehensive vehicle register system (Nasjonalt Materiellregister - NMR) used nationally in Norway and other locations. It serves as the backend for managing vehicle data, including vehicles, vehicle types, deck plans, and vehicle models with full versioning and history tracking.

### Key Information
- **Organization**: Entur AS
- **License**: EUPL-1.2
- **Repository**: https://github.com/entur/sobek
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven
- **Current Version**: 0.0.2-SNAPSHOT

## Technology Stack

### Core Technologies
- **Spring Boot** - Application framework
- **Hibernate 6.5.3** - ORM with spatial support (hibernate-spatial)
- **PostgreSQL/PostGIS** - Database with geospatial extensions
- **Jersey** - REST API framework
- **Jackson** - JSON processing
- **GraphQL** (graphql-java 20.9) - Primary API interface

### Key Dependencies
- **Hazelcast 5.2.5** - Distributed caching and memory grid for multi-instance coordination
- **NeTEx Java Model 2.0.16** - NeTEx format support
- **GeoTools 30.2** - Geospatial operations
- **JTS Core 1.20.0** - Geometry operations
- **Flyway** - Database migrations
- **Testcontainers 1.21.3** - Integration testing with Docker
- **Orika 1.5.4** - Object mapping
- **Google Cloud PubSub** - Event publishing
- **AWS Secrets Manager** - Credential management

### Additional Components
- **Lombok 1.18.42** - Code generation
- **Micrometer/Prometheus** - Metrics and monitoring
- **ActiveMQ** - JMS messaging
- **Rest Assured 5.5.6** - API testing

## Architecture

### Package Structure
```
org.rutebanken.sobek/
├── auth/              - Authentication and authorization
├── autosys/           - Auto systems integration
├── changelog/         - Change event publishing
├── config/            - Spring configuration classes
├── diff/              - Version comparison and diff tools
├── dtoassembling/     - DTO assembly and transformation
├── exporter/          - NeTEx export functionality
├── filter/            - Data filtering utilities
├── general/           - General utilities
├── geo/               - Geospatial operations
├── importer/          - NeTEx import with matching and merging
├── jersey/            - REST API configuration
├── lock/              - Distributed locking
├── model/             - JPA entity models
├── netex/             - NeTEx marshalling/unmarshalling
├── repository/        - JPA repositories
├── rest/              - REST endpoints
├── service/           - Business logic services
├── time/              - Time utilities
└── versioning/        - Entity versioning system
```

### Key Features

#### 1. NeTEx Import/Export
- **Import**: Supports multiple import types (MERGE, INITIAL, ID_MATCH, MATCH)
- **Export**: Both synchronous and asynchronous exports to Google Cloud Storage
- **Validation**: XML schema validation for incoming/outgoing data
- **Format**: http://netex-cen.eu/ standard compliance

#### 2. GraphQL API
- Rich API for vehicles, vehicle types, deck plans, and vehicle models
- Supports queries with filtering (including polygon-based geographic filtering)
- Mutations for create/update operations
- GraphQL processes for operations like merging vehicle types and deck plans
- Available at: `/services/vehicles/graphql`
- GraphiQL UI available for development

#### 3. ID Generation & Mapping
- Gap-less ID generation for NeTEx entities
- Configurable via `netex.validPrefix` property
- Tracks old-to-new ID mappings in database
- Uses Hazelcast for distributed coordination (GaplessIdGeneratorService)
- Handles both auto-generated and externally-assigned IDs

#### 4. Versioning & History
- Full version history for all entities
- Tracks who made changes
- Diff tool for comparing versions
- Audit trail for compliance

#### 5. Multi-Instance Support
- Uses Hazelcast for distributed coordination
- Can run multiple instances in Kubernetes
- Shared state management via memory grid
- Distributed caching with Hazelcast second-level cache

#### 6. Authentication & Authorization
- OAuth2/OIDC support (Keycloak/Auth0)
- JWT token validation
- Role-based access control
- Configurable authorization checks
- See [Keycloak_Setup_Guide.md](./Keycloak_Setup_Guide.md) for setup details

## Database

### Schema Management
- **Flyway** manages migrations
- Migrations located in: `src/main/resources/db/migration/`
- Schema versioning table: `schema_version`
- Automatic execution on application startup

### Spatial Features
- PostGIS extension for geospatial data
- Hibernate Spatial dialect
- Administrative polygon filtering support
- Coordinate reference system handling

### Connection Pooling
- HikariCP for connection pooling
- Configurable via application properties
- Default: 40 max pool size

## Development Setup

### Prerequisites
- Java 21
- Maven 3+
- Docker (for dependencies and integration tests)
- Directory `/deployments/data` with write permissions

### Quick Start - Local Development

1. **Start Dependencies**:
```bash
docker compose up
```
This starts PostGIS at `localhost:37433`

2. **Run Application**:
```bash
mvn spring-boot:run
```
Application available at: `http://localhost:37999`

**IntelliJ**: Right-click `SobekApplication.java` → Run
- Configure profiles: `local,local-blobstore,local-changelog`

### Docker Compose Profiles

Base profile (always active):
- PostGIS database on port 37433

Optional profiles:
- `aws` - LocalStack for AWS feature development (currently commented out)

### Required Spring Profiles

Must select one from each category:

**Storage** (choose one):
- `gcs-blobstore` - Google Cloud Storage
- `local-blobstore` - Local filesystem
- `rutebanken-blobstore` - With sub-profile: `local-disk-blobstore` or `in-memory-blobstore`

**Changelog** (choose one):
- `local-changelog` - Logs to stdout
- `activemq` - JMS ActiveMQ
- `google-pubsub` - GCP PubSub

**Example**: `local,local-blobstore,local-changelog`

### Build & Test

```bash
# Build
mvn clean install

# Run tests (requires Docker for Testcontainers)
mvn test

# Integration tests use Testcontainers with PostgreSQL
```

**Important**: For large imports, may need to increase memory:
```bash
export MAVEN_OPTS='-Xms256m -Xmx1712m -Xss256m -XX:NewSize=64m -XX:MaxNewSize=128m'
```

### Docker Deployment

```bash
# Build JAR
mvn clean install

# Build Docker image
docker build -t sobek .

# Or use Jib for containerization
mvn jib:build
```

**Container Details**:
- Base: eclipse-temurin:21-noble
- Port: 8780
- User: appuser (uid/gid: 2000)
- Data directory: `/deployments/data`

## API Endpoints

### GraphQL
- Endpoint: `/services/vehicles/graphql`
- GraphiQL UI: `https://api.dev.entur.io/graphql-explorer/vehicles`
- Supports queries, mutations, and custom functions

### REST API (NeTEx)

**Synchronous Export**:
```bash
GET /vehicles/v1/netex?size=1000&page=1&allVersions=true
```

**Asynchronous Export**:
```bash
# Initiate
curl https://api.dev.entur.io/vehicles/v1/netex/export/initiate

# Check status
curl https://api.dev.entur.io/vehicles/v1/netex/export

# Download (when complete)
curl https://api.dev.entur.io/vehicles/v1/netex/export/{jobId}/content
```

**Import**:
```bash
curl -XPOST -H "Content-Type: application/xml" \
  -d @vehicle-data.xml \
  http://localhost:1997/services/vehicles/netex
```

### Monitoring
- Actuator endpoints: `/actuator/info`, `/actuator/env`, `/actuator/metrics`
- Prometheus metrics: enabled
- Performance monitoring via Hazelcast (configurable delay)

## Configuration

### Key Application Properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/sobek
spring.datasource.username=sobek
spring.datasource.password=sobek
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL9Dialect
spring.jpa.properties.hibernate.dialect=org.hibernate.spatial.dialect.postgis.PostgisDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.table=schema_version

# Import Types
netex.import.enabled.types=MERGE,INITIAL,ID_MATCH,MATCH

# ID Generation
netex.validPrefix=NSR
netex.profile.version=1.12:NO-NeTEx-stops:1.4

# OAuth2/Security
authorization.enabled=true
sobek.oauth2.resourceserver.auth0.entur.internal.jwt.issuer-uri=http://localhost:8082/realms/entur
sobek.oauth2.resourceserver.auth0.entur.internal.jwt.audience=hathor

# Hazelcast
hazelcast.performance.monitoring.enabled=true
hazelcast.performance.monitoring.delay.seconds=2

# Blob Storage
blobstore.local.folder=/tmp/local-gcs-storage/sobek/export
async.export.path=/tmp

# Changelog
changelog.publish.enabled=false

# Server
server.port=1888
jettyMaxThreads=10
jettyMinThreads=1

# Validation
publicationDeliveryUnmarshaller.validateAgainstSchema=true
publicationDeliveryStreamingOutput.validateAgainstSchema=true
```

## Import Types

1. **INITIAL** - First-time import with parallel processing
   - Requires: `-Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL`
   - Creates new entities with generated IDs
   
2. **MERGE** - Merges with existing data
   - Matches by original ID
   - Updates existing or creates new
   
3. **ID_MATCH** - Matches purely on ID
4. **MATCH** - Custom matching logic

## Frontend

**Hathor** - React-based UI for Sobek
- Repository: https://github.com/entur/hathor
- Provides vehicle management interface
- Integrates with Sobek GraphQL API
- Supports authentication via Keycloak/Auth0

## Database Operations

### Clean Data
```sql
TRUNCATE vehicle CASCADE;
TRUNCATE vehicle_type CASCADE;
TRUNCATE deck_plan CASCADE;
```

### Schema Updates
1. Create migration file in `src/main/resources/db/migration/`
2. Follow naming: `V{version}__{description}.sql`
3. Commit with code changes
4. Auto-executes on startup

## Deployment

### Kubernetes
- Helm charts available in `/helm/sobek/`
- Uses Hazelcast for service discovery
- Configurable via `rutebanken.kubernetes.enabled`
- Multi-instance ready

### Infrastructure
- Terraform configurations in `/terraform/`
- Cloud platform support (GCP focus)
- AWS support via Spring Cloud AWS

## Testing

### Test Structure
- Unit tests: Standard JUnit/Spring Boot Test
- Integration tests: Testcontainers with PostgreSQL
- REST API tests: Rest Assured
- Test profiles configured in `application.properties`

### Running Tests
```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=VehicleImporterTest

# Integration tests (requires Docker)
mvn verify
```

## CI/CD

Workflows located in `.github/workflows/`:
- `ci.yaml` - Continuous integration
- `entur-push.yml` - Push to container registry

## Monitoring & Observability

- **Metrics**: Micrometer with Prometheus registry
- **Logging**: Logback with Logstash encoder (JSON structured logs)
- **Graphite**: Integration for metrics reporting
- **Hazelcast Monitoring**: Performance tracking enabled

## Related Projects

- **Hathor** - Frontend UI (https://github.com/entur/hathor)
- **NeTEx Java Model** - NeTEx data model
- **Rutebanken Helpers** - Shared utilities (storage, OAuth2, organization)
- **Autosys** - Auto systems integration (dependency)

## Troubleshooting

### Common Issues

1. **Build fails - /deployments/data missing**
   - Create directory: `sudo mkdir -p /deployments/data && sudo chmod 777 /deployments/data`

2. **Integration tests fail**
   - Ensure Docker is running for Testcontainers
   - Check Docker socket permissions

3. **Out of memory on large imports**
   - Increase heap: `export MAVEN_OPTS='-Xmx2048m'`

4. **Parallel import authentication errors**
   - Set: `-Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL`

5. **Hazelcast cluster issues**
   - Check network configuration
   - Review `hazelcast.xml` settings
   - Verify Kubernetes service discovery

## Development Tips

1. **GraphiQL** - Use for testing GraphQL queries during development
2. **Swagger** - REST API documentation available via Swagger UI
3. **Hot Reload** - Use Spring Boot DevTools for faster development
4. **Local Profile** - Always include `local` profile for development
5. **Schema Validation** - Can disable for faster dev cycles (not recommended for production)

## License & Contributing

Licensed under EUPL-1.2 with modifications. See [LICENSE.txt](./LICENSE.txt) for details.

Contributions should follow the existing code structure and include appropriate tests.

## Contact & Support

- **Organization**: Entur AS (http://www.entur.org/)
- **Repository Issues**: https://github.com/entur/sobek/issues
- **Documentation**: See README.md and Keycloak_Setup_Guide.md

## Class Diagrams

Vehicle model structure is documented in:
- `vehicle_class_diagram.puml` - PlantUML source
- `vehicle_class_diagram.png` - Visual diagram

---

**Last Updated**: December 2024
**Claude.ai Context**: This document provides comprehensive project context for AI assistants like Claude to understand and work with the Sobek codebase.
