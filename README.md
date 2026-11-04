# Sobek ![Build](https://github.com/entur/sobek/actions/workflows/entur-push.yml/badge.svg)
Module also known as the backend for Vehicle Register ("nasjonalt materiellregister - nmr")

It is used nationally in Norway, and other places.
Sobek is created with technologies like Spring Boot, Hibernate, Postgis, Jersey and Jackson. It is based on the same technology stack as Tiamat, see https://github.com/entur/tiamat

## Core functionality
### NeTEx imports
* Supports different pre steps and merging options for vehicle data (vehicle, vehicle type, deck plan, vehicle model), handling bad data quality.
* Assigns unique IDs to vehicles (if desired).
* Validates incoming data against the XML schema.

### NeTEx exports
Supports exporting vehicles and other entities to the http://netex-cen.eu/ format.
There are many options for exports:
* Asynchronous exports to google cloud storage. Asynchronous exports handles large amount of data, even if exporting thousands of vehicles.
* Synchronous exports directly returned
* Several export parameters and filtering (ex: query or administrative polygons filtering)
* Exports can be validated against the NeTEx schema, ensuring quality.

### GraphQL API
Sobek provides a rich GraphQL API for vehicles, vehicle types, deck plans and so on, support the same parameters as the NeTEx export API.
It also supports mutations. So you can update or create entities.
There are also graphql processes (named functions) which allows functionality like merging vehicle types and deck plans

### Physical and contractual vehicle types
A vehicle type is either `PHYSICAL` or `CONTRACTUAL` (the *kind* of the vehicle type):
* `PHYSICAL`: a group of actual vehicles that exist in the real world. Vehicles refer to physical vehicle types only.
* `CONTRACTUAL`: a theoretical description of the vehicle type that an operator and an authority have agreed on. No vehicle may refer to it.

Rules:
* The kind is required when creating a vehicle type, and cannot be changed afterwards. When updating, omit it or repeat the existing value.
* A vehicle that refers to a contractual vehicle type is rejected.
* Sobek assigns the IDs of new physical vehicle types in the configured codespace (e.g. `NMR:VehicleType:123`), and those of contractual ones in the `CVT` codespace (e.g. `CVT:VehicleType:123`). They share the same number sequence. See [ID Generation](#id-generation).
* NeTEx has no field for the kind, so it is expressed by the codespace of the ID: `CVT` means contractual, anything else means physical.

### A ReactJS Frontend
A frontend for Sobek is available. It's name is Hathor.
See https://github.com/entur/hathor

### Mapping of IDs
After import vehicles and assigning new IDs to vehicles, sobek keeps olds IDs in a mapping table.
The mapping table between old and new IDs is available through the GraphQL API and a REST endpoint.

### Versioning
Vehicles and other entities are versioned. This means that you have full version history of data and what person that made those changes.
Sobek also includes a diff tool. This is used to compare and show the difference between two versions of a vehicle (or other entity).


## Build

```shell
mvn clean install
```

You need the directory `/deployments/data` with rights for the user who
performs the build.

## Integration tests
Sobek uses testcontainers to run integration tests against a real database.  To run Testcontainers-based tests, you need a Docker-API compatible container runtime
for more detail see https://www.testcontainers.org/supported_docker_environment/

(default profiles are set in application.properties)

## Running the service

There are several options for running the service depending on what you need.

 - [Run locally for development](#run-locally-for-development) is for people intending to maintain, modify and improve 
   sobek's source code
 - [Run sobek with Docker compose](#run-sobek-with-docker-compose) if you just need to get the service running
 - [Run with external properties file and PostgreSQL](#run-with-external-properties-file-and-postgresql) for low 
   level debugging

> **Note!** Each of these configurations use unique port numbers and such, be sure to read the provided documentation 
> and configuration files for more details.

## Run locally for development

Local development is a combination of using Docker Compose based configuration for starting up the supporting 
services and running Spring Boot with at least `local` profile enabled.

When running,

 - sobek will be available at `http://localhost:37999`
 - PostGIS will be available at `localhost:37433`

### 1. Start Local Environment through Docker Compose

Sobek has [docker-compose.yml](./docker-compose.yml) which contains all necessary dependent services for running sobek in
various configurations. It is assumed this environment is always running when the service is being run locally
(see below).

> **Note!** This uses the compose version included with modern versions of Docker, not the separately installable
> `docker-compose` command.

All Docker Compose commands run in relation to the `docker-compose.yml` file located in the same directory in which the
command is executed.

```shell
# run with defaults - use ^C to shutdown containers
docker compose up
# run with additional profiles, e.g. with LocalStack based AWS simulator
docker compose --profile aws up
# run in background
docker compose up -d # or --detach
# shutdown containers
docker compose down
# shutdown containers included in specific profile
docker compose --profile aws down
```

#### Supported Docker Compose profiles

Docker Compose has its own profiles which start up additional supporting services to e.g. make specific feature 
development easier. You may include any number of additional profiles when working with Docker Compose by listing 
them in the commands with the `--profile {profile name}` argument. Multiple profiles are activated by providing the 
same attribute multiple times, for example starting Compose environment with profiles a and b would be
```shell
docker compose --profile a --profile b up
```

The provided profiles for Sobek development are


| profile | description                                                                                       |
|:--------|---------------------------------------------------------------------------------------------------|
| `aws`   | Starts up [LocalStack](https://www.localstack.cloud/) meant for developing AWS specific features. |


See [Docker Compose reference](https://docs.docker.com/compose/reference/) for more details.

See [Supported Docker Compose Profiles](#supported-docker-compose-profiles) for more information on provided profiles.

### 2. Run the Service

#### Available Spring Boot Profiles

> **Note!** You must choose at least one of the options from each category below!

> **Note!** `local` profile must always be included!
> **Note!** You need to define the following environment variables:
> - `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_INTERNAL_CLIENT_SECRET`
> - `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_INTERNAL_CLIENT_ID`

##### Storage

| profile                | description                                                                                                                                                     |
|:-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `gcs-blobstore`        | GCP GCS implementation of sobek's blob storage                                                                                                                 |
| `local-blobstore`      | Use local directory as backing storage location.                                                                                                                |
| `rutebanken-blobstore` | Use [`rutebanken-helpers/storage`][rutebanken-storage] based implementation for storage. Must be combined with one of the supported extra profiles (see below). |

[rutebanken-storage]: https://github.com/entur/rutebanken-helpers/tree/master/storage

###### Supported `rutebanken-blobstore` extra profiles

If this profile is chosen, an additional implementation must be chosen to activate the underlying actual implementation.
Supported extra profiles are

| extra profile          | description                              |
|:-----------------------|------------------------------------------|
| `local-disk-blobstore` | Similar to `local-blobstore`.            |
| `in-memory-blobstore`  | Entirely in-memory based implementation. |


**Example: Activating `in-memory-blobstore` for local development**
```properties
spring.profiles.active=local,rutebanken-blobstore,in-memory-blobstore,local-changelog
```

See the [`RutebankenBlobStoreServiceConfiguration`](sobek-app/src/main/java/org/rutebanken/sobek/config/RutebankenBlobStoreConfiguration.java)
class for configuration keys and additional information.

##### Changelog

| profile           | description                                                        |
|:------------------|--------------------------------------------------------------------|
| `local-changelog` | Simple local implementation which logs the sent events to `stdout` |
| `activemq`        | JMS based ActiveMQ implementation.                                 |
| `google-pubsub`   | GCP PubSub implementation for publishing sobek entity changes.    |

#### Supported Docker Compose Profiles

Sobek's [`docker-compose.yml`](./docker-compose.yml) comes with built-in profiles for various use cases. The profiles 
are mostly optional, default profile contains all mandatory configuration while the named profiles add features on 
top of that. You can always activate zero or more profiles at the same time, e.g.

```shell
docker compose --profile first --profile second up
# or
COMPOSE_PROFILES=first,second docker compose up
```

### Default profile (no activation key)

Starts up PostGIS server with settings matching the ones in [`application-local.properties`](./src/main/resources/application-local.properties).


#### Run It!

**IntelliJ**: Right-click on `SobekApplication.java` and choose Run (or Cmd+Shift+F10). Open Run -> Edit 
configurations, choose the correct configuration (Spring Boot -> App), and add a comma separated list of desired 
profiles (e.g. `local,local-blobstore,activemq`) to Active profiles. Save the configuration.

**Command line**:
```bash
mvn clean install -DskipTests && mvn -pl sobek-app spring-boot:run
```
Note: This is a multi-module project. The `install` step is needed to build dependency modules
(`sobek-model`, `sobek-service`, etc.) before running the app module. After the initial build,
you only need to re-run `install` when you change code outside `sobek-app`.

## Run Sobek with Docker compose
To run Sobek with Docker compose, you need to have a docker-compose.yml file. In docker-compose folder you will find a compose.yml file.:

```shell
docker compose up
```

This will start Sobek with PostgreSQL. and you can access Sobek on http://localhost:1888 and the database on http://localhost:5433 
and graphiql on http://localhost:8780/services/vehicles/graphql , At start up sobek copy empty schema to the database. Spring properties are set in application.properties.
Security is disabled in this setup.

## Run with external properties file and PostgreSQL
To run with PostgreSQL you need an external `application.properties`. Below is an example of `application.properties`:

```properties
spring.jpa.database=POSTGRESQL
spring.datasource.platform=postgres
spring.jpa.properties.hibernate.hbm2ddl.import_files_sql_extractor=org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor
spring.jpa.hibernate.hbm2ddl.import_files_sql_extractor=org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor
spring.jpa.hibernate.ddl-auto=none

spring.http.gzip.enabled=true

#spring.jpa.properties.hibernate.format_sql=true

spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.batch_versioned_data=true

spring.flyway.enabled=true
spring.flyway.table=schema_version

server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain

spring.jpa.hibernate.id.new_generator_mappings=true
spring.jpa.hibernate.use-new-id-generator-mappings=true

management.endpoints.web.exposure.include=info,env,metrics
management.endpoints.prometheus.enabled=true
management.metrics.endpoint.export.prometheus.enabled=true

spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.default_batch_fetch_size=16
spring.jpa.properties.hibernate.generate_statistics=false

changelog.publish.enabled=false

spring.datasource.hikari.maximumPoolSize=40
spring.datasource.hikari.leakDetectionThreshold=30000

sobek.locals.language.default=eng

debug=true

# Disable feature detection by this undocumented parameter. Check the org.hibernate.engine.jdbc.internal.JdbcServiceImpl.configure method for more details.
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults = false

# Because detection is disabled you have to set correct dialect by hand.
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL9Dialect

spring.jpa.properties.hibernate.dialect=org.hibernate.spatial.dialect.postgis.PostgisDialect

spring.database.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5436/sobek
spring.datasource.username=sobek
spring.datasource.password=sobek

#OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=https:http://localhost:8082/realms/entur
sobek.oauth2.resourceserver.auth0.entur.internal.jwt.audience=hathor

spring.cloud.gcp.pubsub.enabled=false

server.port=1888

blobstore.gcs.blob.path=exports
blobstore.gcs.bucket.name=sobek-test
blobstore.gcs.project.id=carbon-1287

security.basic.enabled=false
management.security.enabled=false
authorization.enabled = true
rutebanken.kubernetes.enabled=false

async.export.path=/tmp

publicationDeliveryUnmarshaller.validateAgainstSchema=false
publicationDeliveryStreamingOutput.validateAgainstSchema=false
netex.validPrefix=NSR
netex.profile.version=2.0:NO-NeTEx:2.0
blobstore.local.folder=/tmp/local-gcs-storage/sobek/export
spring.profiles.active=local-blobstore,activemq
```

To start Sobek with this configuration, specify **spring.config.location**:

`java -Dspring.config.location=/path/to/sobek.properties -Denv=dev -jar sobek.jar`

## Database

### HikariCP
Sobek is using HikariCP. Most properties should be be possible to be specified in in application.properties, like `spring.datasource.initializationFailFast=false`. More information here. https://github.com/brettwooldridge/HikariCP/wiki/Configuration
See also http://stackoverflow.com/a/26514779

## ID Generation
### Background
What Sobek does with an ID it receives (from Hathor, from an organisation's own system, in a NeTEx import) depends on
the codespace of the ID, the first part of `<codespace>:<type>:<number>`:

| Codespace | Who assigns IDs | What Sobek does with an incoming ID |
|---|---|---|
| **NMR** (the configured `netex.validPrefix`), and **CVT** for contractual vehicle types | Only Sobek | An ID of an object that exists is an **update** of that object. An ID that does not exist is **rejected** (HTTP 400 / `BAD_REQUEST`): clients cannot create new IDs here. |
| A codespace in `netex.valid.codespaces.list`, e.g. `AKT` | The client | The ID is respected and used as the NeTEx ID. Sobek never assigns IDs here. The same ID again is an update. |
| Any other codespace, typically another system such as Autosys | The other system | Sobek assigns a new ID in NMR (CVT for a contractual vehicle type) and keeps the external ID as the key value `imported-id`. The same external ID again is an update of the same object. |

For objects without an ID, Sobek assigns an ID in NMR (CVT for a contractual vehicle type). Contractual and physical vehicle
types share one number sequence.

The `CVT` codespace is Sobek's for contractual vehicle types only: a physical vehicle type cannot have an ID in it, and a contractual vehicle type cannot have an ID in `NMR`.

## Keycloak/Auth0
Both Sobek and Hathor are set up to be used with Keycloak or Auth0.
A detailed guide on how to setup Keycloak can be found [here](./Keycloak_Setup_Guide.md).

## Validation for incoming and outgoing NeTEx publication delivery

It is possible to configure if sobek should validate incoming and outgoing NeTEx xml when unmarshalling or marshalling publication deliveries.
Default values are true. Can be deactivated with setting properties to false.
```properties
publicationDeliveryStreamingOutput.validateAgainstSchema=false
publicationDeliveryUnmarshaller.validateAgainstSchema=true
```

## Synchronous NeTEx export with query params
It is possible to export vehicles and related data directly to NeTEx format. This is the endpoint:
https://api.dev.entur.io/vehicles/v1/netex

### Required parameter: vehicleTypeKind
`vehicleTypeKind` is required. Acceptable values are `PHYSICAL`, `CONTRACTUAL` and `BOTH`. A request without it is rejected with status 400.
```http request
GET https://api.dev.entur.io/vehicles/v1/netex?vehicleTypeKind=PHYSICAL
```
* `PHYSICAL`: physical vehicle types, and the vehicles that refer to them.
* `CONTRACTUAL`: contractual vehicle types only. Vehicles are not exported, as they only refer to physical vehicle types.
* `BOTH`: both kinds of vehicle types, and vehicles.

### Data owner
`dataOwner` restricts the export to objects owned by that organisation, e.g. `?dataOwner=ATU&vehicleTypeKind=BOTH`.
Administrators may omit it, which means all data owners.

## Async NeTEx export from Sobek
Asynchronous export uploads exported data to google cloud storage. When initiated, you will get a job ID back.
When the job is finished, you can download the exported data.

*Most of the parameters from synchronous export works with asynchronous export as well!*

### Start async export:
```
curl https://api.dev.entur.io/vehicles/v1/netex/export/initiate
```
Pro tip: Pipe the output from curl to xmllint to format the output:
```
curl https://api.dev.entur.io/vehicles/v1/netex/export/initiate | xmllint --format -
```

### Check job status:
```
curl https://api.dev.entur.io/vehicles/v1/netex/export
```

### When job is done. Download it:
```
curl https://api.dev.entur.io/vehicles/v1/netex/export/130116/content | zcat | xmllint --format - > export.xml
```

## Truncate data in sobek database
Clean existing data in postgresql (streamline if frequently used):
```
TRUNCATE vehicle CASCADE;
TRUNCATE vehicle_type CASCADE;
TRUNCATE deck_plan CASCADE;
```

## Import data into Sobek

If you are running this from `spring:run`, then you need to make sure that you have enough memory available for the java process (in case of large data sets).
Another issue is thread stack size, which might need to be increased when coping with really large NeTEx imports.
Example:
```
export MAVEN_OPTS='-Xms256m -Xmx1712m -Xss256m -XX:NewSize=64m -XX:MaxNewSize=128m -Dfile.encoding=UTF-8'
```

### Import NeTEx file without *NMR* IDs
This NeTEx file should not contain NMR ID. (The NMR prefix is configurable in the class ValidPrefixList)
* Sobek will match existing vehicles, vehicle types, vehicle models and deck plans based on their original ID.

Sobek will return the modified NeTEx structure with it's own NSR IDs. Original IDs will be present in key value list on each object.

See [ID Generation](#id-generation) for what happens to IDs in the import: IDs in NMR/CVT must exist (they are updates), IDs in registered codespaces are used as they are, and other IDs are kept as `imported-id`.

NeTEx has no field for the kind of a vehicle type, so the import takes a required parameter, `vehicleTypeKind=PHYSICAL|CONTRACTUAL` (400 if it is missing or invalid). It is the kind of every vehicle type in the file whose ID is not in the `CVT` codespace: those in registered codespaces (e.g. `AKT`) and those with another system's ID, which Sobek gives an ID of its own (`CVT` for contractual). A vehicle type with a `CVT` ID is contractual whatever the parameter says. A file therefore holds one kind (plus `CVT` IDs). A new contractual vehicle type cannot have a `CVT` ID in the file, as only Sobek assigns those. An existing vehicle type keeps its kind: an import that implies another kind is rejected.

```shell
curl -XPOST -H"Content-Type: application/xml" -d@my-nice-netex-file.xml "http://localhost:1997/services/vehicles/netex?vehicleTypeKind=PHYSICAL"
```

## GraphQL
GraphQL endpoint is available on
```
https://api.dev.entur.io/vehicles/v1/graphql
```

Vehicle types and the `VehicleTypeKind` enum (`PHYSICAL`, `CONTRACTUAL`):
* The `vehicleTypes` query requires `vehicleTypeKind` in its filter, e.g. `vehicleTypes(filter: { dataOwnerRef: "ATU", vehicleTypeKind: PHYSICAL })`.
* `createOrUpdateVehicleType` requires `vehicleTypeKind` when creating. It cannot be changed afterwards; omit it on update.
* `createOrUpdateVehicle` rejects a `transportType` that is a contractual vehicle type.

Tip: GraphiQL UI available on https://api.dev.entur.io/graphql-explorer/vehicles using *GraphiQL*:
https://github.com/graphql/graphiql
(Use e.g. `Modify Headers` for Chrome to add bearer-token for mutations)

### Schema file
The current GraphQL schema (`schema.graphqls`) is published to GitHub Pages
on every change merged to `main`:
https://entur.github.io/sobek/

Use it for client codegen, IDE tooling and drift checks.

## Flyway
To create the database for sobek, download and use the flyway command line tool:
https://flywaydb.org/documentation/commandline/

### Migrations
Migrations are executed when sobek is started.

### Schema changes
Create a new file according to the flyway documentation in the folder `resources/db/migrations`.
Commit the migration together with code changes that requires this schema change. Follow the naming convention.

Tip: If you have run a migration on your local database and need to make a small change to it, you have two options to make the change:
1. Manually reverse the migration and run it again
2. Do the change  in your local database and then (to fix flyway status):
```
   mvn flyway:repair -Dflyway.url=jdbc:postgresql://localhost:37433/sobek   -Dflyway.user=sobek   -Dflyway.password=<password for sobek user> -Dflyway.table=schema_version
```

## Organisation registry

Sobek needs an organisation registry in order to populate authority and operator references. Entur will use Baba/Ninkasi as organisation registry.
The organisation registry needs to provide at least ID, name and type of each organisation.


```properties
nanna.organisations.uri=<path-to-organisation-registry-api>
```

You can also provide your own implementation of the [`OrganisationRegistry`](sobek-common/src/main/java/org/rutebanken/sobek/organisation/OrganisationRegistry.java)
interface.

