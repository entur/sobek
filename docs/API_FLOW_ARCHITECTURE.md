# Sobek API Flow Architecture

> Reverse-engineered architectural overview of Sobek's API layer.
> Generated: January 2025

## Overview

Sobek exposes two primary API interfaces:
- **Jersey (JAX-RS)** - REST endpoints for NeTEx XML import/export
- **GraphQL** - Query API for vehicle data (mutations currently disabled)

This document describes 5 distinct API flows, their dependencies, and data transformation layers.

---

## Heavy Dependencies Summary

| Dependency | Purpose | Used In |
|------------|---------|---------|
| **Orika** (`ma.glasnost.orika`) | Bidirectional mapping: JPA entities ↔ NeTEx model | Import, Export, Autosys |
| **netex-java-model** (`org.rutebanken.netex.model`) | JAXB-generated NeTEx XML bindings | All flows except GraphQL |
| **JAXB** (`jakarta.xml.bind`) | XML marshalling/unmarshalling | Import, Export, Autosys |
| **graphql-java** | GraphQL schema & execution | GraphQL Read |
| **Hibernate Spatial** | PostGIS integration | All flows (persistence) |
| **Hazelcast** | Distributed ID generation, caching | Import (versioning) |
| **BlobStoreService** | GCS/local file storage | Async Export |
| **autosys** (`org.entur.autosys`) | Norwegian vehicle registry client | Autosys Integration |

---

## Flow 1: Jersey Query (REST GET - Sync Export)

Synchronous NeTEx XML export via streaming response.

### Mermaid Diagram

```mermaid
flowchart LR
    subgraph Client
        HTTP["HTTP Client<br/>GET /netex"]
    end

    subgraph Jersey["Jersey REST Layer"]
        VER["VehicleExportResource<br/>@Path @GET"]
        ER["ExportResource<br/>(alternative)"]
    end

    subgraph Export["Export Layer"]
        SPD["StreamingPublicationDelivery<br/>@Component"]
        PDC["PublicationDeliveryCreator"]
        PDSO["PublicationDeliveryStreamingOutput"]
    end

    subgraph Mapping["Orika Mapping"]
        NM["NetexMapper<br/>66 converters/mappers"]
    end

    subgraph Persistence["Repository Layer"]
        VR["VehicleRepository"]
        VTR["VehicleTypeRepository"]
        DPR["DeckPlanRepository"]
        VMR["VehicleModelRepository"]
    end

    subgraph DB["Database"]
        PG[(PostgreSQL<br/>PostGIS)]
    end

    HTTP -->|"GET /vehicles/v1/netex<br/>?size=1000&page=1"| VER
    VER --> SPD
    ER --> SPD
    SPD -->|"Lazy scroll"| VR
    SPD -->|"Lazy scroll"| VTR
    SPD -->|"Lazy scroll"| DPR
    SPD -->|"Lazy scroll"| VMR
    VR --> PG
    VTR --> PG
    DPR --> PG
    VMR --> PG
    SPD -->|"JPA Entity"| NM
    NM -->|"NeTEx Model"| PDC
    PDC --> PDSO
    PDSO -->|"StreamingOutput<br/>XML"| HTTP
```

### Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `VehicleExportResource` | `rest/netex/publicationdelivery/VehicleExportResource.java` | Entry point, `@GET` |
| `StreamingPublicationDelivery` | `exporter/StreamingPublicationDelivery.java` | Lazy entity iteration |
| `NetexMapper` | `netex/mapping/NetexMapper.java` | Orika facade |
| `PublicationDeliveryStreamingOutput` | `rest/netex/.../PublicationDeliveryStreamingOutput.java` | JAXB marshalling |

### Dependencies Introduced

| Dependency | Why |
|------------|-----|
| **Orika** | Maps `Vehicle` (JPA) → `Vehicle` (NeTEx) |
| **netex-java-model** | Target type for mapping, JAXB serialization |
| **JAXB** | `Marshaller.marshal()` for XML output |
| **Hibernate ScrollableResults** | Memory-efficient large dataset export |

### Orika Mappers Used

```
VehicleMapper, VehicleTypeMapper, VehicleModelMapper, DeckPlanMapper,
DeckMapper, PassengerSpaceMapper, PassengerSpotMapper, + 20 converters
```

---

## Flow 2: Jersey Command (REST POST - Import)

NeTEx XML import with entity versioning and ID generation.

### Mermaid Diagram

```mermaid
flowchart LR
    subgraph Client
        HTTP["HTTP Client<br/>POST /netex"]
    end

    subgraph Jersey["Jersey REST Layer"]
        VIR["VehicleImportResource<br/>@Path @POST"]
        IR["ImportResource<br/>(alternative)"]
    end

    subgraph Unmarshalling["XML Processing"]
        PDU["PublicationDeliveryUnmarshaller<br/>JAXB + Schema Validation"]
    end

    subgraph Import["Import Orchestration"]
        PDI["PublicationDeliveryImporter<br/>@Service"]
    end

    subgraph Handlers["Import Handlers"]
        VIH["VehicleImportHandler"]
        VTIH["VehicleTypeImportHandler"]
        VMIH["VehicleModelImportHandler"]
        DPIH["DeckPlanImportHandler"]
    end

    subgraph Importers["Entity Importers"]
        VI["VehicleImporter"]
        VTI["VehicleTypeImporter"]
        VMI["VehicleModelImporter"]
        DPI["DeckPlanImporter"]
    end

    subgraph Mapping["Orika Mapping"]
        NM["NetexMapper<br/>NeTEx → JPA"]
    end

    subgraph Versioning["Versioning Layer"]
        VVSS["VehicleVersionedSaverService"]
        VC["VersionCreator"]
        NIA["NetexIdAssigner<br/>Hazelcast"]
    end

    subgraph Persistence["Repository Layer"]
        VR["VehicleRepository"]
        VTR["VehicleTypeRepository"]
    end

    subgraph DB["Database"]
        PG[(PostgreSQL)]
    end

    HTTP -->|"POST XML<br/>Content-Type: application/xml"| VIR
    VIR --> PDU
    IR --> PDU
    PDU -->|"PublicationDeliveryStructure"| PDI
    PDI --> VIH
    PDI --> VTIH
    PDI --> VMIH
    PDI --> DPIH
    VIH --> VI
    VTIH --> VTI
    VMIH --> VMI
    DPIH --> DPI
    VI -->|"NeTEx Vehicle"| NM
    NM -->|"JPA Vehicle"| VVSS
    VVSS --> VC
    VVSS --> NIA
    VVSS --> VR
    VR --> PG
```

### Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `VehicleImportResource` | `rest/netex/.../VehicleImportResource.java:78-105` | Entry point, `@POST` |
| `PublicationDeliveryUnmarshaller` | `rest/netex/.../PublicationDeliveryUnmarshaller.java` | JAXB unmarshal + XSD validation |
| `PublicationDeliveryImporter` | `importer/PublicationDeliveryImporter.java:81+` | Handler orchestration |
| `VehicleImporter` | `importer/VehicleImporter.java:53-75` | Per-entity import logic |
| `VehicleVersionedSaverService` | `versioning/save/VehicleVersionedSaverService.java` | Version creation |
| `NetexIdAssigner` | `netex/id/NetexIdAssigner.java` | Gap-less ID generation |

### Dependencies Introduced

| Dependency | Why |
|------------|-----|
| **Orika** | Maps `Vehicle` (NeTEx) → `Vehicle` (JPA) |
| **netex-java-model** | Source type from XML, JAXB deserialization |
| **JAXB** | `Unmarshaller.unmarshal()` with schema validation |
| **Hazelcast** | Distributed ID generation via `GaplessIdGeneratorService` |
| **Hibernate** | `EntityManager.persist()` with version tracking |

### Import Types Supported

```java
enum ImportType {
    MERGE,      // Match + merge existing
    INITIAL,    // Fresh import, parallel processing
    ID_MATCH,   // Pure ID lookup (default for Sobek)
    MATCH       // ID + geographic matching
}
```

### Orika Mapping Direction

```
NeTEx XML → JAXB Unmarshal → org.rutebanken.netex.model.Vehicle
         → Orika NetexMapper.map(netexVehicle, JPA Vehicle.class)
         → org.rutebanken.sobek.model.vehicle.Vehicle (JPA Entity)
         → Repository.save()
```

---

## Flow 3: GraphQL Read (Queries)

GraphQL query execution returning JSON. **Read-only** - no mutations implemented.

### Mermaid Diagram

```mermaid
flowchart LR
    subgraph Client
        HTTP["GraphQL Client<br/>POST /graphql"]
    end

    subgraph Jersey["Jersey REST Layer"]
        GR["GraphQLResource<br/>@Path @POST"]
    end

    subgraph GraphQL["GraphQL Engine"]
        RGQS["RegisterGraphQLSchema<br/>vehicleRegisterSchema"]
        GE["graphql.execute()<br/>ExecutionInput"]
    end

    subgraph Fetchers["Data Fetchers"]
        VTF["VehicleTypeFetcher"]
        VTDF["VehicleTypeDeckPlanFetcher"]
        UPF["UserPermissionsFetcher"]
        KVF["KeyValuesDataFetcher"]
        TF["TagFetcher"]
    end

    subgraph Persistence["Repository Layer"]
        VTR["VehicleTypeRepository"]
        DPR["DeckPlanRepository"]
    end

    subgraph DB["Database"]
        PG[(PostgreSQL)]
    end

    HTTP -->|"POST JSON<br/>{query: '...'}"| GR
    GR -->|"getGraphQLResponseInTransaction"| GE
    GE --> RGQS
    RGQS -->|"fieldDefinition"| VTF
    RGQS -->|"fieldDefinition"| VTDF
    RGQS -->|"fieldDefinition"| UPF
    VTF -->|"findAllCurrent()"| VTR
    VTDF -->|"findByNetexId()"| DPR
    VTR --> PG
    DPR --> PG
    VTF -->|"JPA Entity"| GE
    GE -->|"ExecutionResult<br/>JSON"| HTTP
```

### Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `GraphQLResource` | `rest/graphql/GraphQLResource.java:127-154` | Entry point, `@POST /graphql` |
| `RegisterGraphQLSchema` | `rest/graphql/RegisterGraphQLSchema.java:143-165` | Schema definition |
| `VehicleTypeFetcher` | `rest/graphql/fetchers/VehicleTypeFetcher.java:42-57` | Query resolver |
| `VehicleTypeDeckPlanFetcher` | `rest/graphql/fetchers/VehicleTypeDeckPlanFetcher.java` | Nested field resolver |

### Dependencies Introduced

| Dependency | Why |
|------------|-----|
| **graphql-java** | Schema definition, query execution, type system |
| **Hibernate** | Direct JPA entity queries (no Orika needed) |
| **Spring @Transactional** | Read transaction for consistent results |

### GraphQL Does NOT Use Orika

Unlike Jersey flows, GraphQL returns JPA entities directly (serialized to JSON by Jackson). No NeTEx model conversion occurs.

```
GraphQL Query → DataFetcher → Repository.findAll() → JPA Entities → Jackson JSON
```

### Available Queries

```graphql
type Query {
    vehicleRegister: VehicleRegister
}

type VehicleRegister {
    vehicleTypes(
        ids: [String]
        size: Int = 20
        page: Int = 0
    ): [VehicleType]
    userPermissions: UserPermissions
}
```

---

## Flow 4: GraphQL Command (Mutations) - DISABLED

**Status: NOT IMPLEMENTED**

GraphQL mutations are explicitly disabled in `RegisterGraphQLSchema.java:163`:

```java
vehicleRegisterSchema = GraphQLSchema.newSchema()
    .query(vehicleRegistryQuery)
//  .mutation(stopPlaceRegisterMutation)  // <-- COMMENTED OUT
    .codeRegistry(buildCodeRegistry(vehicleTypeResolver))
    .build();
```

### Mermaid Diagram

```mermaid
flowchart LR
    subgraph Client
        HTTP["GraphQL Client<br/>POST mutation"]
    end

    subgraph Jersey["Jersey REST Layer"]
        GR["GraphQLResource"]
    end

    subgraph GraphQL["GraphQL Engine"]
        RGQS["RegisterGraphQLSchema"]
        DISABLED["MUTATIONS DISABLED<br/>Line 163: commented out"]
    end

    HTTP -->|"POST mutation"| GR
    GR --> RGQS
    RGQS --> DISABLED
    DISABLED -->|"No mutation handlers<br/>registered"| HTTP

    style DISABLED fill:#ffcccc,stroke:#cc0000
```

### Why Disabled?

Sobek was forked from Tiamat. The mutation infrastructure exists in code but was never wired for vehicle entities. All write operations must go through the **Jersey REST import endpoint**.

### If Mutations Were Enabled, They Would Require

| Dependency | Why |
|------------|-----|
| **Orika** | Would need bidirectional mapping for input types |
| **Hazelcast** | ID generation for new entities |
| **Versioning** | Would need `VersionedSaverService` integration |

---

## Flow 5: Async Export Job API

Long-running export jobs with polling and blob storage.

### Mermaid Diagram

```mermaid
flowchart TB
    subgraph Client
        HTTP["HTTP Client"]
    end

    subgraph Jersey["Jersey REST Layer"]
        AER["AsyncExportResource<br/>@Path"]
        INIT["GET /initiate"]
        LIST["GET /export"]
        STATUS["GET /{id}/status"]
        CONTENT["GET /{id}/content"]
    end

    subgraph Export["Async Export Service"]
        APDE["AsyncPublicationDeliveryExporter<br/>@Service"]
        EJW["ExportJobWorker<br/>ThreadPool(3)"]
    end

    subgraph Mapping["Orika Mapping"]
        NM["NetexMapper"]
        NMI["NetexMappingIterator<br/>Lazy streaming"]
    end

    subgraph Storage["External Storage"]
        BS["BlobStoreService<br/>GCS / Local"]
        LOCAL["/deployments/data/"]
    end

    subgraph Persistence["Repository Layer"]
        EJR["ExportJobRepository"]
        VR["VehicleRepository"]
    end

    subgraph DB["Database"]
        PG[(PostgreSQL)]
    end

    HTTP -->|"1. GET /initiate"| INIT
    INIT --> APDE
    APDE -->|"Create ExportJob<br/>status=QUEUED"| EJR
    EJR --> PG
    APDE -->|"Submit to pool"| EJW

    EJW -->|"Background"| VR
    VR --> PG
    EJW -->|"Stream entities"| NMI
    NMI --> NM
    NM -->|"NeTEx XML"| BS
    BS --> LOCAL
    EJW -->|"Update status=FINISHED"| EJR

    HTTP -->|"2. GET /export"| LIST
    LIST --> APDE
    APDE -->|"findAll()"| EJR

    HTTP -->|"3. GET /{id}/status"| STATUS
    STATUS --> APDE
    APDE -->|"findById()"| EJR

    HTTP -->|"4. GET /{id}/content"| CONTENT
    CONTENT --> APDE
    APDE -->|"Read file"| BS
    BS -->|"InputStream"| HTTP
```

### Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `AsyncExportResource` | `rest/netex/.../AsyncExportResource.java:44-102` | 4 REST endpoints |
| `AsyncPublicationDeliveryExporter` | `exporter/AsyncPublicationDeliveryExporter.java` | Job lifecycle |
| `ExportJobWorker` | `rest/netex/.../async/ExportJobWorker.java` | Background processing |
| `NetexMappingIterator` | `exporter/async/NetexMappingIterator.java` | Lazy Orika mapping |
| `BlobStoreService` | `service/BlobStoreService.java` | GCS/local storage |

### Dependencies Introduced

| Dependency | Why |
|------------|-----|
| **Orika** | Streaming entity → NeTEx conversion |
| **netex-java-model** | JAXB marshalling to XML file |
| **BlobStoreService** | File storage abstraction (GCS/local) |
| **ExecutorService** | Fixed thread pool (3 threads) for background jobs |
| **Guava ThreadFactoryBuilder** | Named threads for debugging |

### Job Status Flow

```
QUEUED → PROCESSING → FINISHED
                   ↘ FAILED (on error)
```

### Why Separate Flow?

- **Not synchronous** - returns job ID immediately
- **Requires polling** - client checks status endpoint
- **External storage** - results in GCS/filesystem, not HTTP response
- **Thread pool isolation** - doesn't block request threads

---

## Flow 6: Autosys Integration (External API Bridge)

Special endpoint bridging Norwegian government vehicle registry (Autosys/Statens vegvesen) to NeTEx format.

### Mermaid Diagram

```mermaid
flowchart LR
    subgraph Client
        HTTP["HTTP Client<br/>GET /autosys"]
    end

    subgraph Jersey["Jersey REST Layer"]
        AAR["AutosysAPIResource<br/>@Path @GET"]
    end

    subgraph External["External Service"]
        AVS["AutosysVehicleService<br/>HTTP Client"]
        AUTOSYS["Autosys API<br/>Statens vegvesen"]
    end

    subgraph Mapping["Custom Mapping"]
        MS["MapperService<br/>@Service"]
        NM["NetexMapper"]
    end

    subgraph Export["Export Layer"]
        SRFE["SobekResourceFrameExporter"]
        SCFE["SobekCompositeFrameExporter"]
        PDC["PublicationDeliveryCreator"]
    end

    subgraph Output["Response"]
        PDSO["PublicationDeliveryStreamingOutput"]
    end

    HTTP -->|"GET /autosys<br/>?registrationNumber=AB12345"| AAR
    AAR --> AVS
    AVS -->|"HTTP GET"| AUTOSYS
    AUTOSYS -->|"Kjoretoydata<br/>(Vehicle data)"| AVS
    AVS --> MS
    MS -->|"Map Autosys → Sobek"| MS
    MS --> SRFE
    MS --> SCFE
    SRFE --> NM
    SCFE --> NM
    NM -->|"NeTEx Model"| PDC
    PDC --> PDSO
    PDSO -->|"StreamingOutput<br/>XML"| HTTP
```

### Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `AutosysAPIResource` | `rest/netex/.../AutosysAPIResource.java:56-62` | Entry point |
| `AutosysVehicleService` | External `org.entur.autosys` library | HTTP client |
| `MapperService` | `autosys/MapperService.java` | Autosys → NeTEx transformation |
| `SobekResourceFrameExporter` | `exporter/SobekResourceFrameExporter.java` | Frame creation |

### Dependencies Introduced

| Dependency | Why |
|------------|-----|
| **org.entur.autosys** | External library for Autosys API client |
| **Orika (NetexMapper)** | Maps Sobek entities to NeTEx model |
| **netex-java-model** | Output format |
| **JAXB** | XML marshalling |

### Why Separate Flow?

- **External data source** - fetches from government API, not local database
- **Custom mapping** - `Kjoretoydata` → `Vehicle` (not standard Orika)
- **One-off transformation** - doesn't persist, just converts format
- **No versioning** - external data, no local state management

### Autosys Data Model

```java
// From org.entur.autosys.model
Kjoretoydata {
    String registrationNumber;
    TekniskeData tekniskeData;  // Technical specifications
    List<Kode> codes;           // Classification codes
}
```

---

## Summary: Flow Comparison

| Flow | Entry | Orika | netex-java-model | Persistence | Threading |
|------|-------|-------|------------------|-------------|-----------|
| Jersey Query | `@GET /netex` | JPA → NeTEx | Marshal | Read | Request |
| Jersey Command | `@POST /netex` | NeTEx → JPA | Unmarshal | Write + Version | Request |
| GraphQL Read | `@POST /graphql` | None | None | Read | Request |
| GraphQL Command | N/A | N/A | N/A | N/A | N/A |
| Async Export | `@GET /export/*` | JPA → NeTEx | Marshal | Read + Job state | ThreadPool(3) |
| Autosys | `@GET /autosys` | Sobek → NeTEx | Marshal | None (external) | Request |

---

## Orika Mapper Inventory

The `NetexMapper` component (`netex/mapping/NetexMapper.java`) registers:

### Entity Mappers (29)
```
VehicleMapper, VehicleTypeMapper, VehicleModelMapper, DeckPlanMapper,
DeckMapper, DeckSpaceCapacityMapper, PassengerSpaceMapper, PassengerSpotMapper,
PassengerEntranceMapper, PassengerCapacityStructureMapper, SpotRowMapper,
SpotColumnMapper, SpotAffinityMapper, SpotEquipmentMapper, SeatEquipmentMapper,
BedEquipmentMapper, LuggageSpotMapper, LuggageSpotEquipmentMapper,
AccessVehicleEquipmentMapper, EntranceEquipmentMapper, StaircaseEquipmentMapper,
SchematicMapMapper, SchematicMapMemberMapper, AccessibilityAssessmentMapper,
DataManagedObjectStructureMapper, EntityInVersionStructureMapper,
KeyListToKeyValuesMapMapper, MultilingualStringMapper, VehicleEquipmentProfileMemberMapper
```

### Type Converters (27)
```
DeckListConverter, DeckSpaceCapacityListConverter, PassengerSpaceListConverter,
PassengerSpotListConverter, PassengerEntranceListConverter, SpotRowListConverter,
SpotColumnListConverter, SpotAffinityListConverter, LuggageSpotListConverter,
EquipmentListConverter, SchematicMapListConverter, VehicleEquipmentProfileMemberListConverter,
PassengerCapacitySetConverter, AccessibilityLimitationsListConverter,
AlternativeNamesConverter, KeyValuesToKeyListConverter, ValidBetweenConverter,
SimplePointVersionStructureConverter, PolygonConverter, LineStringConverter,
DurationConverter, LocalDateTimeInstantConverter, OffsetDateTimeInstantConverter,
ZonedDateTimeInstantConverter, DeckPlanRefConverter, VehicleModelRefConverter,
TransportTypeRefConverter, + Equipment Ref Converters (8)
```

---

## Architecture Notes

### What This Document Ignores

Per the analysis scope, the following cross-cutting concerns are excluded:

- Authentication/Authorization interceptors
- Hazelcast caching layer (except ID generation)
- Error handling and validation
- Logging and metrics
- Transaction management details
- Spring Security filter chain

### Code Locations

All paths relative to `src/main/java/org/rutebanken/sobek/`:

```
rest/
├── graphql/
│   ├── GraphQLResource.java
│   ├── RegisterGraphQLSchema.java
│   └── fetchers/*.java
└── netex/publicationdelivery/
    ├── VehicleImportResource.java
    ├── VehicleExportResource.java
    ├── AsyncExportResource.java
    ├── AutosysAPIResource.java
    └── async/*.java

importer/
├── PublicationDeliveryImporter.java
├── handler/*.java
├── converter/*.java
└── *Importer.java

exporter/
├── StreamingPublicationDelivery.java
├── AsyncPublicationDeliveryExporter.java
└── async/*.java

netex/mapping/
├── NetexMapper.java
├── mapper/*.java
└── converter/*.java
```
