# NeTEx Module Extraction — Implementation Prompt

> Self-contained prompt for implementing the netex module extraction from Sobek.
> Can be used on any branch — files may have been reorganized by coworkers.
> Generated from analysis session on 2026-01-27.

---

## Objective

Extract NeTEx marshalling, import, and export concerns from the Sobek monolith into four reusable Maven modules. The modules must be publishable artifacts that sibling project Tiamat can also consume. Follow a "do one thing" module philosophy.

## Current State

Sobek is a single-module Spring Boot 3.x / Java 21 application:
- **groupId:** `org.rutebanken`, **artifactId:** `sobek`, **version:** `0.0.2-SNAPSHOT`
- **parent:** `org.entur.ror:superpom`
- **packaging:** `jar` (monolithic, no `<modules>`)
- ~463 Java source files, ~46 test files

### Key Packages

| Package | Files | Role |
|---------|-------|------|
| `model/` | ~193 | JPA entities (Vehicle, VehicleType, DeckPlan, Equipment, etc.) |
| `netex/mapping/` | ~81 | Orika mappers/converters for NeTEx ↔ JPA bidirectional mapping |
| `rest/` | ~54 | Jersey REST + GraphQL endpoints |
| `importer/` | ~27 | NeTEx import orchestration, handlers, ID converters |
| `exporter/` | ~15 | NeTEx export, async export, streaming |
| `repository/` | ~28 | JPA repositories |
| `versioning/` | ~15 | Entity versioning |

### Architecture Flow

```
REST Resource → PublicationDeliveryUnmarshaller → PublicationDeliveryImporter → Handlers → Importers → Repositories
                                                                                    ↕
                                                                              NetexMapper (Orika)
                                                                                    ↕
                                                                            JPA Domain Model
```

Export flow:
```
REST Resource → StreamingPublicationDelivery → Repositories → NetexMapper → JAXB Marshaller
```

### Critical Dependencies Between Classes

- **Import → Export:** `PublicationDeliveryImporter` uses `PublicationDeliveryCreator` (from exporter package) to build response envelopes. This is the only cross-direction dependency.
- **Export → Import:** None.
- **Both share:** `NetexMapper` (Orika), `PublicationDeliveryHelper`, `PublicationDeliveryCreator`
- **Orika mapping layer** (NetexMapper + 61 converters + 26 mappers) is tightly coupled to Sobek's JPA domain model. It does real structural transformation: ID routing/validation, reference dereferencing, multilingual string normalization, key-value format conversion. It is NOT trivial field copying.

### Known Dead Code

- `ImportResource.java` — identical duplicate of `VehicleImportResource.java`, not registered in `JerseyConfig`
- `ExportResource.java` vs `VehicleExportResource.java` — potential duplicate, verify registration
- `PublicationDeliveryHelper` contains Tiamat leftover methods referencing SiteFrame/StopPlace concepts unused in Sobek

### Classes With Zero Sobek Domain Coupling (verified)

These can be extracted without any dependency on `org.rutebanken.sobek.model`:
1. `PublicationDeliveryHelper.java` (after removing SiteFrame methods)
2. `PublicationDeliveryCreator.java` (depends only on ValidPrefixList + netex-java-model)
3. `PublicationDeliveryUnmarshaller.java` (JAXB only)
4. `PublicationDeliveryStreamingOutput.java` (JAXB + JAX-RS StreamingOutput)
5. `NetexMappingContext.java` (stdlib only)
6. `NetexMappingContextThreadLocal.java` (stdlib only)
7. `NetexMappingException.java` (stdlib only)
8. `ValidPrefixList.java` + related `netex/id/` classes

### Import Response Behavior

The import POST does NOT echo back the input. It returns the **persisted state** mapped back to NeTEx — reflecting generated IDs, version numbers, and server-side modifications. The `importParams.skipOutput=true` flag returns empty 200 OK instead.

---

## Target Module Structure

```
sobek/
├── pom.xml                          (parent, packaging=pom)
├── sobek-app/                       (Spring Boot shell + Orika impls + REST resources + JPA)
└── sobek-modules/
    └── netex/
        ├── pom.xml                  (aggregator, packaging=pom)
        ├── netex-api/               (interfaces, value types, constants)
        ├── netex-commons/           (shared NeTEx utilities, zero domain coupling)
        ├── netex-import/            (import orchestration)
        └── netex-export/            (export orchestration)
```

### Module Dependency Graph

```
netex-api              ← only netex-java-model + jakarta
    ↑
netex-commons          ← netex-api + JAXB + guava
    ↑          ↑
netex-import  netex-export   ← netex-commons + spring-context
    ↑          ↑
sobek-app              ← all modules + Orika + JPA + repositories
```

Import and export NEVER depend on each other. Orika stays in sobek-app behind SPI interfaces.

---

## Implementation Steps

### Step 0: Prerequisite Cleanup

**Before any restructuring**, clean up in one PR:

1. **Delete `ImportResource.java`** (verify it's still dead code — not in JerseyConfig). The real import endpoint is `VehicleImportResource`.
2. **Resolve Export resource duplication** — check if `ExportResource` and `VehicleExportResource` are both registered. Keep only the active one.
3. **Remove Tiamat leftover methods** from `PublicationDeliveryHelper` — any methods referencing `SiteFrame`, `StopPlace`, `TopographicPlace`, `TariffZone`, `PathLink`, `Parking`, `GroupOfTariffZones`.
4. **Extract `IMPORT_CORRELATION_ID`** from `PublicationDeliveryImporter` into a standalone `ImportConstants` class.

**How to find these files if reorganized:** Search for:
- `class ImportResource` with `@Path("netex")` and `@POST`
- `class VehicleImportResource` — same pattern
- Methods in `PublicationDeliveryHelper` containing "SiteFrame" or "Stop"
- `IMPORT_CORRELATION_ID` string constant

**Verify:** `mvn test`

### Step 1: Convert to Multi-Module POM

**This is the riskiest step — do in isolation.**

1. Root `pom.xml`:
   - Change `<packaging>jar</packaging>` → `<packaging>pom</packaging>`
   - Add `<modules>` listing `sobek-app` and `sobek-modules/netex`
   - Keep all `<dependencyManagement>` and `<properties>` in root
   - Remove `spring-boot-maven-plugin` from root (moves to sobek-app)
   - Remove `jib-maven-plugin` from root (moves to sobek-app)

2. Create `sobek-app/pom.xml`:
   - `<parent>` = root sobek
   - Inherits all current dependencies (copy dependency section from root initially)
   - `spring-boot-maven-plugin` with repackage execution
   - `jib-maven-plugin` for container building

3. Move all `src/` into `sobek-app/src/`

4. Create `sobek-modules/netex/pom.xml` — aggregator only, `<packaging>pom</packaging>`

5. Create empty placeholder POMs for `netex-api`, `netex-commons`, `netex-import`, `netex-export`

6. Update `Dockerfile` COPY path if it references `target/`

7. Update CI workflows (`.github/workflows/`) if they reference specific paths

**Verify:** `mvn clean install` from root. All tests pass. `mvn spring-boot:run -pl sobek-app` boots the application.

### Step 2: Create netex-api

Package: `org.rutebanken.netex.api`

**New interfaces:**

```java
public interface NetexPersistenceService {
    /** Unmarshal, import, and optionally marshal response */
    PublicationDeliveryStructure importDelivery(InputStream xml, ImportParams params)
        throws IOException, JAXBException, SAXException;
}

public interface NetexMarshallingService {
    /** Marshal a PublicationDeliveryStructure for HTTP streaming */
    StreamingOutput marshal(PublicationDeliveryStructure delivery) throws JAXBException;
}

public interface EntityMapper {
    /** Map source object to destination type */
    <S, D> D map(S source, Class<D> destinationClass);
    <S, D> List<D> mapAsList(List<S> source, Class<D> destinationClass);
}
```

**Moved files:**
- `ImportType.java` (from `importer/` package)
- `ImportParams.java` (from `importer/` package)
- `ImportConstants.java` (created in Step 0)

**POM dependencies:** `netex-java-model`, `jakarta.ws.rs-api`

**How to find these files if reorganized:** Search for `enum ImportType`, `class ImportParams`, `IMPORT_CORRELATION_ID`.

**Verify:** Update imports in sobek-app. `mvn test`.

### Step 3: Create netex-commons

Package: `org.rutebanken.netex.commons`

**Moved files** (find by class name if paths changed):
- `PublicationDeliveryHelper` — NeTEx frame extraction utilities
- `PublicationDeliveryCreator` — envelope builder (currently in exporter package — moving here fixes import→export dependency)
- `PublicationDeliveryUnmarshaller` — JAXB XML → PublicationDeliveryStructure
- `PublicationDeliveryStreamingOutput` — PublicationDeliveryStructure → XML streaming
- `NetexMappingContext` — thread-local context (ZoneId holder)
- `NetexMappingContextThreadLocal` — ThreadLocal wrapper
- `NetexMappingException` — custom exception
- `ValidPrefixList` + related classes from `netex/id/` package

**Tests moved:** `PublicationDeliveryUnmarshallerTest`, `PublicationDeliveryStreamingOutputTest`, `PublicationDeliveryTestHelper`

**POM dependencies:** `netex-api`, `netex-java-model`, `jakarta.xml.bind-api`, `jakarta.ws.rs-api`, `guava`, `spring-context`

**Verify:** `mvn test`

### Step 4: Create netex-import

Package: `org.rutebanken.netex.importing`

**Moved files** (entire importer/ package minus ImportType and ImportParams already moved):
- `PublicationDeliveryImporter` — core orchestrator
- All import handlers: `VehicleImportHandler`, `VehicleTypeImportHandler`, `DeckPlanImportHandler`, `EquipmentImportHandler`, `VehicleModelImportHandler`, `SchematicMapImportHandler`
- All importers: `VehicleImporter`, `VehicleTypeImporter`, `DeckPlanImporter`, `EquipmentImporter`, `SchematicMapImporter`
- All ID converters from `importer/converter/`
- `OriginalIdMatcher` from `importer/matching/`
- `ImportLogger`, `ImportLoggerTask` from `importer/log/`
- `WordsRemover` from `importer/modifier/`
- `KeyValueListAppender`

**Orika decoupling:** Refactor import handlers to use `EntityMapper` (from netex-api) instead of `NetexMapper.getFacade()`. In sobek-app, create `OrikaEntityMapper implements EntityMapper` that delegates to `NetexMapper`.

**Repository decoupling:** Importers currently call versioned saver services directly. Define saver interfaces in netex-api:
```java
public interface VersionedEntitySaver<T> {
    T saveNewVersion(T entity);
}
```
sobek-app provides implementations backed by real JPA services.

**POM dependencies:** `netex-api`, `netex-commons`, `spring-context`

**Verify:** `mvn test`

### Step 5: Create netex-export

Package: `org.rutebanken.netex.exporting`

**Moved files** (entire exporter/ package minus PublicationDeliveryCreator already in commons):
- `StreamingPublicationDelivery` — core export engine
- `AsyncPublicationDeliveryExporter` — async job management
- Frame exporters: `SobekResourceFrameExporter`, `SobekServiceFrameExporter`, `SobekComositeFrameExporter`
- `PublicationDeliveryStructurePage`, `SobekPublicationDeliveryExportException`
- `async/`: `ExportJobWorker`, `NetexMappingIterator`, `NetexMappingIteratorList`
- `eviction/`: `EntitiesEvictor`, `SessionEntitiesEvictor`
- `params/`: `ExportParams`, `SearchObject`

**Repository decoupling:** Define `NetexExportDataProvider` interface in netex-api aggregating all data retrieval:
```java
public interface NetexExportDataProvider {
    Iterator<Vehicle> scrollVehicles(ExportParams params);
    Iterator<VehicleType> scrollVehicleTypes(ExportParams params);
    // etc.
}
```
sobek-app implements with real repositories. `StreamingPublicationDeliveryConfig` stays in sobek-app to wire concrete repos into the provider.

**POM dependencies:** `netex-api`, `netex-commons`, `spring-context`

**Verify:** `mvn test`

### Step 6: Wire REST Resources to Interfaces

Final wiring in sobek-app:

1. **Create `NetexPersistenceServiceImpl`** in sobek-app — delegates to `PublicationDeliveryUnmarshaller` + `PublicationDeliveryImporter` + `PublicationDeliveryStreamingOutput`
2. **Create `OrikaEntityMapper implements EntityMapper`** — delegates to `NetexMapper.getFacade()`
3. **Refactor `VehicleImportResource`** to inject `NetexPersistenceService` instead of the three individual classes
4. **Refactor `AutosysAPIResource`** to inject `NetexMarshallingService`
5. **Refactor export resources** to inject export interfaces
6. **Update `JerseyConfig`** if resource class names/packages changed

**Verify:** `mvn clean install` from root. Full test suite. Manual smoke test of import/export if possible.

---

## Orika Strategy

The `EntityMapper` SPI in netex-api cleanly decouples all modules from Orika:

- **Now:** sobek-app provides `OrikaEntityMapper` wrapping `NetexMapper`
- **Future (MapStruct):** sobek-app provides `MapStructEntityMapper` — netex modules unchanged
- **Future (orm.xml):** if mapping moves to JPA layer, `EntityMapper` impl simplifies to pass-through

The ~88 Orika files (NetexMapper + 61 converters + 26 mappers) all stay in sobek-app until replacement happens. Module boundaries remain stable regardless of which mapping approach wins.

---

## What Tiamat Can Reuse

After publishing to artifact repository:
- **`netex-api`** — service interfaces, `ImportType`, `ImportParams`, `EntityMapper` SPI
- **`netex-commons`** — unmarshalling, marshalling, envelope building, helper utilities, validation prefix handling

Tiamat implements `EntityMapper`, `VersionedEntitySaver`, and `NetexExportDataProvider` with its own domain model and persistence layer.

---

## Test Distribution

| Module | Tests | Spring Context? | DB? |
|--------|-------|-----------------|-----|
| netex-api | None (interfaces only) | No | No |
| netex-commons | Unmarshaller, StreamingOutput | No | No |
| netex-import | Matchers, converters, word remover (mock EntityMapper) | No | No |
| netex-export | Iterators (mock data provider) | No | No |
| sobek-app | All integration tests | Yes (Testcontainers) | Yes |

---

## Verification Checklist (every step)

- [ ] `mvn clean install` from root succeeds
- [ ] All existing tests pass
- [ ] No circular module dependencies (`mvn dependency:tree` clean)
- [ ] `netex-api` has zero `org.rutebanken.sobek` imports
- [ ] `netex-commons` has zero `org.rutebanken.sobek.model` imports
- [ ] App boots: `mvn spring-boot:run -pl sobek-app`
- [ ] CI workflows still function (check `.github/workflows/`)

---

## Notes for Working on a Different Commit

Files may have been moved, renamed, or reorganized by coworkers. Use these search strategies:

| To find | Search for |
|---------|-----------|
| Import resource | `class.*ImportResource` with `@Path("netex")` and `@POST` |
| Export resource | `class.*ExportResource` with `@Path("netex")` and `@GET` |
| Jersey config | `class.*JerseyConfig` or `ResourceConfig` |
| Streaming export config | `StreamingPublicationDelivery` bean creation |
| Orika mapper | `class NetexMapper` or `MapperFacade` |
| Publication delivery helpers | `class PublicationDelivery(Helper|Creator|Unmarshaller|StreamingOutput)` |
| Import handlers | `class.*ImportHandler` with `@Component` |
| ID converters | `class.*IdConverter` in importer package |
| Domain models | `@Entity` annotations in `model/` |
| Flyway migrations | `src/main/resources/db/migration/V*` |

Always verify JerseyConfig registration before deleting any REST resource class.
