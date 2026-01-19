# Orika to MapStruct Migration Plan

## Summary

| Category | Files | Lines of Code |
|----------|-------|---------------|
| **Main mapping code** | 69 | 4,699 |
| **Test code** | 9 | 966 |
| **Other (VersionCreator)** | 1 | ~10 LOC affected |
| **Total** | **79 files** | **~5,665 LOC** |

---

## Breakdown by Component Type

| Type | Count | Total LOC | Avg LOC/file |
|------|-------|-----------|--------------|
| **Converters** (`BidirectionalConverter`) | 37 | ~2,100 | 57 |
| **Mappers** (`CustomMapper`) | 26 | ~1,200 | 46 |
| **Core classes** (`NetexMapper`, etc.) | 6 | ~900 | 150 |
| **Tests** | 9 | 966 | 107 |

---

## Key Files (by complexity)

| File | LOC | Complexity |
|------|-----|------------|
| `NetexMapper.java` | 399 | **High** - Central orchestrator, 30+ class mappings |
| `PublicationDeliveryHelper.java` | 193 | Medium |
| `SimplePointVersionStructureConverter.java` | 169 | Medium - Geo conversion |
| `TagKeyValuesMapper.java` | 163 | Medium |
| `PolygonConverter.java` | 161 | Medium - Geo conversion |
| `NetexIdMapper.java` | 154 | Medium |
| `DataManagedObjectStructureMapper.java` | 152 | Medium |

---

## Orika Patterns Used

### 1. BidirectionalConverter<A, B> - 37 files

Example: `DeckPlanRefConverter` (converts NeTEx refs to Sobek entities)

```java
@Component
public class DeckPlanRefConverter extends BidirectionalConverter<DeckPlanRefStructure, DeckPlan> {
    private final ReferenceResolver resolver;

    @Override
    public DeckPlan convertTo(DeckPlanRefStructure ref, Type<DeckPlan> type, MappingContext ctx) {
        return resolver.resolve(new VersionOfObjectRefStructure(ref.getRef(), ref.getVersion()), DeckPlan.class);
    }

    @Override
    public DeckPlanRefStructure convertFrom(DeckPlan deckPlan, Type<DeckPlanRefStructure> type, MappingContext ctx) {
        return new DeckPlanRefStructure()
                .withRef(deckPlan.getNetexId())
                .withVersion(String.valueOf(deckPlan.getVersion()));
    }
}
```

Some converters inject `ReferenceResolver` for DB lookups.

### 2. CustomMapper<A, B> - 26 files

Example: `VehicleMapper` (handles `mapAtoB` and `mapBtoA`)

```java
public class VehicleMapper extends CustomMapper<Vehicle, org.rutebanken.sobek.model.vehicle.Vehicle> {

    @Override
    public void mapAtoB(Vehicle vehicle, org.rutebanken.sobek.model.vehicle.Vehicle vehicle2, MappingContext context) {
        super.mapAtoB(vehicle, vehicle2, context);
        if(vehicle.getVehicleModelRef() != null) {
            vehicle2.setVehicleModel(mapperFacade.map(vehicle.getVehicleModelRef(), VehicleModel.class, context));
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle, Vehicle netexVehicle, MappingContext context) {
        super.mapBtoA(sobekVehicle, netexVehicle, context);
        if (sobekVehicle.getTransportType() != null) {
            netexVehicle.withTransportTypeRef(objectFactory.createTransportTypeRef(...));
        }
    }
}
```

Uses `mapperFacade` internally for nested mappings.

### 3. MapperFactory.classMap().byDefault().register() - in NetexMapper.java

30+ class mappings with `.exclude()`, `.field()`, `.customize()`:

```java
mapperFactory.classMap(Vehicle.class, org.rutebanken.sobek.model.vehicle.Vehicle.class)
        .exclude("transportTypeRef")
        .exclude("vehicleTypeRef")
        .customize(new VehicleMapper())
        .byDefault()
        .register();
```

### 4. MapperFacade.map() - in VersionCreator.java

Used for entity copying/cloning:

```java
EntityInVersionStructure copy = defaultMapperFacade.map(entityInVersionStructure, type);
```

---

## Migration Challenges

| Challenge | Impact |
|-----------|--------|
| **Bidirectional mapping** | MapStruct needs explicit `@InheritInverseConfiguration` |
| **Runtime type mapping** | Orika's `byDefault()` -> MapStruct needs explicit field mappings |
| **Injected converters** | `ReferenceResolver` in converters needs rethinking |
| **`MappingContext`** | Orika passes context; MapStruct uses `@Context` |
| **Nested `mapperFacade.map()`** | Must wire MapStruct mappers together |

---

## Files Affected

### Converters (37 files)

Located in `src/main/java/org/rutebanken/sobek/netex/mapping/converter/`:

- `AccessVehicleEquipmentRefConverter.java`
- `AccessibilityLimitationsListConverter.java`
- `AlternativeNamesConverter.java`
- `BedEquipmentRefConverter.java`
- `DeckListConverter.java`
- `DeckPlanRefConverter.java`
- `DeckSpaceCapacityListConverter.java`
- `DurationConverter.java`
- `EntranceEquipmentRefConverter.java`
- `EquipmentListConverter.java`
- `KeyValuesToKeyListConverter.java`
- `LineStringConverter.java`
- `LocalDateTimeInstantConverter.java`
- `LuggageSpotEquipmentRefConverter.java`
- `LuggageSpotListConverter.java`
- `OffsetDateTimeInstantConverter.java`
- `PassengerCapacitySetConverter.java`
- `PassengerEntranceListConverter.java`
- `PassengerSpaceListConverter.java`
- `PassengerSpotListConverter.java`
- `PolygonConverter.java`
- `SchematicMapListConverter.java`
- `SeatEquipmentRefConverter.java`
- `SimplePointVersionStructureConverter.java`
- `SpotAffinityListConverter.java`
- `SpotColumnListConverter.java`
- `SpotEquipmentRefConverter.java`
- `SpotRowListConverter.java`
- `StaircaseEquipmentRefConverter.java`
- `TransportTypeRefConverter.java`
- `ValidBetweenConverter.java`
- `VehicleEquipmentProfileMemberListConverter.java`
- `VehicleModelRefConverter.java`
- `ZonedDateTimeInstantConverter.java`

### Mappers (26 files)

Located in `src/main/java/org/rutebanken/sobek/netex/mapping/mapper/`:

- `AccessVehicleEquipmentMapper.java`
- `AccessibilityAssessmentMapper.java`
- `BedEquipmentMapper.java`
- `DataManagedObjectStructureMapper.java`
- `DeckMapper.java`
- `DeckPlanMapper.java`
- `DeckSpaceCapacityMapper.java`
- `EntityInVersionStructureMapper.java`
- `EntranceEquipmentMapper.java`
- `KeyListToKeyValuesMapMapper.java`
- `LuggageSpotEquipmentMapper.java`
- `LuggageSpotMapper.java`
- `MultilingualStringMapper.java`
- `NetexIdMapper.java`
- `PassengerCapacityStructureMapper.java`
- `PassengerEntranceMapper.java`
- `PassengerSpaceMapper.java`
- `PassengerSpotMapper.java`
- `SchematicMapMapper.java`
- `SchematicMapMemberMapper.java`
- `SeatEquipmentMapper.java`
- `SpotAffinityMapper.java`
- `SpotColumnMapper.java`
- `SpotEquipmentMapper.java`
- `SpotRowMapper.java`
- `StaircaseEquipmentMapper.java`
- `TagKeyValuesMapper.java`
- `VehicleEquipmentProfileMemberMapper.java`
- `VehicleMapper.java`
- `VehicleModelMapper.java`
- `VehicleTypeMapper.java`

### Core Classes (6 files)

- `NetexMapper.java` (399 LOC) - Central orchestrator
- `PublicationDeliveryHelper.java` (193 LOC)
- `EquipmentMappingHelper.java` (112 LOC)
- `NetexMappingContextThreadLocal.java` (80 LOC)
- `NetexMappingContext.java` (24 LOC)
- `NetexMappingException.java` (25 LOC)

### Other Affected Files

- `VersionCreator.java` - Uses `MapperFacade` for entity cloning

### Test Files (9 files)

Located in `src/test/java/org/rutebanken/sobek/netex/mapping/`:

- `mapper/KeyListToKeyValuesMapMapperTest.java` (57 LOC)
- `mapper/TagKeyValuesMapperTest.java` (125 LOC)
- `mapper/NetexIdMapperTest.java` (137 LOC)
- `mapper/DataManagedObjectStructureMapperTest.java` (107 LOC)
- `converter/SimplePointVersionStructureConverterTest.java` (159 LOC)
- `converter/KeyValuesToKeyListConverterTest.java` (63 LOC)
- `converter/PolygonConverterTest.java` (183 LOC)
- `converter/LineStringConverterTest.java` (85 LOC)
- `converter/OffsetDateTimeInstantConverterTest.java` (50 LOC)

---

## Estimated Effort

| Phase | Files | Effort |
|-------|-------|--------|
| 1. Create MapStruct interfaces | ~35 new mapper interfaces | Medium |
| 2. Migrate converters | 37 converters -> `@Mapping` or custom methods | High |
| 3. Migrate CustomMappers | 26 mappers -> `@AfterMapping`/`@BeforeMapping` | High |
| 4. Refactor `NetexMapper.java` | 1 file, orchestrate all mappers | Medium |
| 5. Update `VersionCreator.java` | 1 file, minimal | Low |
| 6. Update tests | 9 files | Medium |

**Total: ~79 files to modify/replace, ~5,665 LOC affected**

---

## MapStruct Equivalents

| Orika Pattern | MapStruct Equivalent |
|---------------|---------------------|
| `BidirectionalConverter` | `@Mapper` with `@InheritInverseConfiguration` |
| `CustomMapper.mapAtoB()` | `@AfterMapping` or `@BeforeMapping` |
| `mapperFacade.map()` | Inject other `@Mapper` interfaces |
| `MappingContext` | `@Context` parameter |
| `.byDefault()` | Implicit (MapStruct maps same-name fields) |
| `.exclude("field")` | `@Mapping(target = "field", ignore = true)` |
| `.field("a", "b")` | `@Mapping(source = "a", target = "b")` |
| `.customize(mapper)` | `@AfterMapping` / `@BeforeMapping` |

---

## Recommended Migration Strategy

1. **Add MapStruct dependency** alongside Orika (parallel operation)
2. **Start with leaf converters** (no dependencies on other mappers)
3. **Migrate simple type converters** (Duration, DateTime, etc.)
4. **Migrate entity mappers** bottom-up (referenced entities first)
5. **Refactor `NetexMapper`** to use MapStruct mappers
6. **Update `VersionCreator`** last
7. **Remove Orika dependency** after all tests pass

---

*Generated: January 2025*
