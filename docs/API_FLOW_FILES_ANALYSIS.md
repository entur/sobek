# API Flow Files Analysis

> Analysis of Java files that solely operate for each API flow.
> Generated: January 2025

## Summary

| Flow | Main Files | Main LOC | Test Files | Test LOC | Total LOC |
|------|------------|----------|------------|----------|-----------|
| Flow 1: Sync Export | 2 | 115 | 0 | 0 | 115 |
| Flow 2: Import | 30 | 2,177 | 4 | 257 | 2,434 |
| Flow 3: GraphQL Read | 34 | 2,627 | 3 | 318 | 2,945 |
| Flow 4: GraphQL Mutations | 0 | 0 | 0 | 0 | 0 (disabled) |
| Flow 5: Async Export | 8 | 745 | 1 | 41 | 786 |
| Flow 6: Autosys | 2 | 513 | 1 | 64 | 577 |
| **Total (flow-specific)** | **76** | **6,177** | **9** | **680** | **6,857** |

---

## Flow 1: Jersey Query (Sync Export)

REST GET endpoint for synchronous NeTEx XML export.

### src/main/java (2 files, 115 LOC)

| File | Path | LOC |
|------|------|----:|
| `VehicleExportResource.java` | `rest/netex/publicationdelivery/` | 43 |
| `ExportResource.java` | `rest/netex/publicationdelivery/` | 72 |

### src/test/java (0 files)

No dedicated test files for sync export.

### Notes
- Most sync export logic is in **shared classes** used by multiple flows:
  - `StreamingPublicationDelivery.java` (shared with async export)
  - `PublicationDeliveryCreator.java` (shared with autosys)
  - `SobekResourceFrameExporter.java` (shared with autosys)

---

## Flow 2: Jersey Command (Import)

REST POST endpoint for NeTEx XML import with entity versioning.

### src/main/java (30 files, 2,177 LOC)

#### REST Resources (3 files, 326 LOC)

| File | Path | LOC |
|------|------|----:|
| `VehicleImportResource.java` | `rest/netex/publicationdelivery/` | 117 |
| `ImportResource.java` | `rest/netex/publicationdelivery/` | 123 |
| `PublicationDeliveryUnmarshaller.java` | `rest/netex/publicationdelivery/` | 86 |

#### Importer Package (27 files, 1,851 LOC)

| File | Path | LOC |
|------|------|----:|
| `PublicationDeliveryImporter.java` | `importer/` | 155 |
| `VehicleImporter.java` | `importer/` | 76 |
| `VehicleTypeImporter.java` | `importer/` | 72 |
| `VehicleModelImporter.java` | `importer/` | 76 |
| `DeckPlanImporter.java` | `importer/` | 76 |
| `EquipmentImporter.java` | `importer/` | 72 |
| `SchematicMapImporter.java` | `importer/` | 76 |
| `ImportType.java` | `importer/` | 50 |
| `ImportParams.java` | `importer/` | 27 |
| `KeyValueListAppender.java` | `importer/` | 45 |
| `VehicleImportHandler.java` | `importer/handler/` | 68 |
| `VehicleTypeImportHandler.java` | `importer/handler/` | 105 |
| `VehicleModelImportHandler.java` | `importer/handler/` | 93 |
| `DeckPlanImportHandler.java` | `importer/handler/` | 92 |
| `EquipmentImportHandler.java` | `importer/handler/` | 114 |
| `SchematicMapImportHandler.java` | `importer/handler/` | 94 |
| `VehicleIdConverter.java` | `importer/converter/` | 35 |
| `VehicleTypeIdConverter.java` | `importer/converter/` | 42 |
| `VehicleModelIdConverter.java` | `importer/converter/` | 35 |
| `DeckPlanIdConverter.java` | `importer/converter/` | 36 |
| `EquipmentIdConverter.java` | `importer/converter/` | 35 |
| `SchematicMapIdConverter.java` | `importer/converter/` | 35 |
| `GenericIdConverter.java` | `importer/converter/` | 76 |
| `OriginalIdMatcher.java` | `importer/matching/` | 109 |
| `WordsRemover.java` | `importer/modifier/` | 64 |
| `ImportLogger.java` | `importer/log/` | 26 |
| `ImportLoggerTask.java` | `importer/log/` | 67 |

### src/test/java (4 files, 257 LOC)

| File | Path | LOC |
|------|------|----:|
| `PublicationDeliveryImporterTest.java` | `importer/` | 67 |
| `KeyValueListAppenderTest.java` | `importer/` | 63 |
| `OriginalIdMatcherTest.java` | `importer/matching/` | 54 |
| `WordsRemoverTest.java` | `importer/modifier/` | 73 |

---

## Flow 3: GraphQL Read (Queries)

GraphQL query execution returning JSON. Read-only - no mutations implemented.

### src/main/java (34 files, 2,627 LOC)

#### Core (6 files, 914 LOC)

| File | Path | LOC |
|------|------|----:|
| `GraphQLResource.java` | `rest/graphql/` | 268 |
| `RegisterGraphQLSchema.java` | `rest/graphql/` | 292 |
| `GraphQLNames.java` | `rest/graphql/` | 202 |
| `RequestLoggingInstrumentation.java` | `rest/graphql/` | 50 |
| `SobekExceptionMaxDepth.java` | `rest/graphql/` | 51 |
| `SobekExceptionWhileDataFetching.java` | `rest/graphql/` | 51 |

#### Fetchers (10 files, 472 LOC)

| File | Path | LOC |
|------|------|----:|
| `VehicleTypeFetcher.java` | `rest/graphql/fetchers/` | 58 |
| `VehicleTypeDeckPlanFetcher.java` | `rest/graphql/fetchers/` | 41 |
| `VehicleTypeVehicleFetcher.java` | `rest/graphql/fetchers/` | 40 |
| `TagFetcher.java` | `rest/graphql/fetchers/` | 75 |
| `UserPermissionsFetcher.java` | `rest/graphql/fetchers/` | 51 |
| `KeyValuesDataFetcher.java` | `rest/graphql/fetchers/` | 49 |
| `OriginalIdsDataFetcher.java` | `rest/graphql/fetchers/` | 35 |
| `ReferenceFetcher.java` | `rest/graphql/fetchers/` | 45 |
| `PolygonFetcher.java` | `rest/graphql/fetchers/` | 34 |
| `PrivateCodeFetcher.java` | `rest/graphql/fetchers/` | 44 |

#### Types (7 files, 483 LOC)

| File | Path | LOC |
|------|------|----:|
| `CustomGraphQLTypes.java` | `rest/graphql/types/` | 150 |
| `VehicleTypeObjectTypeCreator.java` | `rest/graphql/types/` | 63 |
| `VehicleObjectTypeCreator.java` | `rest/graphql/types/` | 52 |
| `DeckPlanObjectTypeCreator.java` | `rest/graphql/types/` | 49 |
| `TagObjectTypeCreator.java` | `rest/graphql/types/` | 83 |
| `EntityRefObjectTypeCreator.java` | `rest/graphql/types/` | 48 |
| `VersionLessEntityRef.java` | `rest/graphql/types/` | 38 |

#### Scalars (2 files, 265 LOC)

| File | Path | LOC |
|------|------|----:|
| `CustomScalars.java` | `rest/graphql/scalars/` | 166 |
| `DateScalar.java` | `rest/graphql/scalars/` | 99 |

#### Mappers (5 files, 288 LOC)

| File | Path | LOC |
|------|------|----:|
| `GroupOfEntitiesMapper.java` | `rest/graphql/mappers/` | 86 |
| `AlternativeNameMapper.java` | `rest/graphql/mappers/` | 60 |
| `ValidBetweenMapper.java` | `rest/graphql/mappers/` | 57 |
| `IdMapper.java` | `rest/graphql/mappers/` | 50 |
| `EmbeddableMultilingualStringMapper.java` | `rest/graphql/mappers/` | 35 |

#### Helpers, Resolvers, Operations (4 files, 205 LOC)

| File | Path | LOC |
|------|------|----:|
| `TagOperationsBuilder.java` | `rest/graphql/operations/` | 83 |
| `CleanupHelper.java` | `rest/graphql/helpers/` | 55 |
| `MutableTypeResolver.java` | `rest/graphql/resolvers/` | 36 |
| `KeyValueWrapper.java` | `rest/graphql/helpers/` | 31 |

### src/test/java (3 files, 318 LOC)

| File | Path | LOC |
|------|------|----:|
| `CustomScalarsTest.java` | `rest/graphql/scalars/` | 142 |
| `DateScalarTest.java` | `rest/graphql/scalars/` | 95 |
| `CustomGraphQLTypesTest.java` | `rest/graphql/types/` | 81 |

---

## Flow 4: GraphQL Mutations - DISABLED

Mutations are explicitly disabled in `RegisterGraphQLSchema.java:163`.

**Files: 0** - No files solely for this flow since it's not implemented.

---

## Flow 5: Async Export Job API

Long-running export jobs with polling and blob storage.

### src/main/java (8 files, 745 LOC)

| File | Path | LOC |
|------|------|----:|
| `AsyncExportResource.java` | `rest/netex/publicationdelivery/` | 102 |
| `AsyncPublicationDeliveryExporter.java` | `exporter/` | 149 |
| `ExportJobWorker.java` | `exporter/async/` | 144 |
| `NetexMappingIterator.java` | `exporter/async/` | 109 |
| `NetexMappingIteratorList.java` | `exporter/async/` | 40 |
| `ExportJob.java` | `model/job/` | 155 |
| `JobStatus.java` | `model/job/` | 22 |
| `ExportJobRepository.java` | `repository/` | 24 |

### src/test/java (1 file, 41 LOC)

| File | Path | LOC |
|------|------|----:|
| `ExportJobTest.java` | `model/job/` | 41 |

---

## Flow 6: Autosys Integration

External API bridge to Norwegian government vehicle registry (Statens vegvesen).

### src/main/java (2 files, 513 LOC)

| File | Path | LOC |
|------|------|----:|
| `AutosysAPIResource.java` | `rest/netex/publicationdelivery/` | 64 |
| `MapperService.java` | `autosys/` | 449 |

### src/test/java (1 file, 64 LOC)

| File | Path | LOC |
|------|------|----:|
| `MapperServiceTest.java` | `autosys/` | 64 |

---

## Shared Infrastructure (Not Counted Above)

These files are used by multiple flows and are not solely attributable to any single flow:

### Export Shared Classes
- `StreamingPublicationDelivery.java` - Sync export, async export
- `PublicationDeliveryCreator.java` - Sync export, autosys, import (response)
- `SobekResourceFrameExporter.java` - Sync export, autosys
- `SobekComositeFrameExporter.java` - Sync export, autosys
- `SobekServiceFrameExporter.java` - Sync export, autosys
- `PublicationDeliveryStreamingOutput.java` - Multiple flows
- `ExportParams.java` - All export flows, autosys, GraphQL

### NeTEx Mapping (Orika)
- `netex/mapping/*.java` - Used by import, sync export, async export, autosys

### Cross-Cutting Concerns (excluded from analysis)
- Authentication/Authorization
- Versioning infrastructure
- Repository layer (shared)
- Configuration classes

---

## Dead/Orphan Code Identified

| File | Location | Issue |
|------|----------|-------|
| `NetexReferenceRemovingIteratorTest.java` | `test/.../exporter/async/` | Tests commented out; references `StopPlace` (Tiamat); main class doesn't exist |
| `rest/.../async/EntityQueueProcessor.java` | `rest/netex/publicationdelivery/async/` | Async import infrastructure (5 files) - appears unused |
| `rest/.../async/PublicationDeliveryPartialUnmarshaller.java` | `rest/netex/publicationdelivery/async/` | References `StopPlace` (Tiamat domain) |

---

## Related Documentation

- [API_FLOW_ARCHITECTURE.md](./API_FLOW_ARCHITECTURE.md) - Flow diagrams and architecture overview
