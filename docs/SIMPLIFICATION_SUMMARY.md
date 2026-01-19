# Sobek Simplification Summary

This document summarizes two major simplification opportunities for the Sobek codebase.

---

## Overview

| Initiative | Files Affected | LOC Impact | Complexity Reduction |
|------------|----------------|------------|----------------------|
| **Orika → MapStruct** | 79 files | ~5,665 LOC to rewrite | Medium (modernization) |
| **Remove Hazelcast/GaplessID** | 17 files to remove, 10 to modify | ~1,270 LOC removed | High (removes distributed state) |
| **Combined Total** | ~100 files | ~6,900 LOC | Significant |

---

## 1. Orika to MapStruct Migration

**Goal:** Replace deprecated Orika mapping library with compile-time MapStruct.

### Scope

| Category | Files | Lines of Code |
|----------|-------|---------------|
| Converters (`BidirectionalConverter`) | 37 | ~2,100 |
| Mappers (`CustomMapper`) | 26 | ~1,200 |
| Core classes (`NetexMapper`, etc.) | 6 | ~900 |
| Tests | 9 | 966 |
| **Total** | **79 files** | **~5,665 LOC** |

### Key Files

| File | LOC | Complexity |
|------|-----|------------|
| `NetexMapper.java` | 399 | High - Central orchestrator |
| `PublicationDeliveryHelper.java` | 193 | Medium |
| `PolygonConverter.java` | 161 | Medium - Geo conversion |
| `DataManagedObjectStructureMapper.java` | 152 | Medium |

### Migration Challenges

- Bidirectional mapping requires `@InheritInverseConfiguration`
- Orika's runtime `byDefault()` → MapStruct needs explicit mappings
- Converters with `ReferenceResolver` injection need rethinking
- Nested `mapperFacade.map()` calls must wire MapStruct mappers together

**Details:** See [ORIKA_TO_MAPSTRUCT_MIGRATION.md](./ORIKA_TO_MAPSTRUCT_MIGRATION.md)

---

## 2. Hazelcast & GaplessID Removal

**Goal:** Remove distributed state coordination, make endpoints stateless.

### Scope

| Category | Files | Lines of Code |
|----------|-------|---------------|
| Files to REMOVE completely | 14 main + 3 test | ~1,254 |
| Files to MODIFY | 10 | ~200 LOC changes |
| Config/Resources to REMOVE | 3 | ~15 |
| **Total Removable** | **17 files** | **~1,270 LOC** |

### Key Removals

| File | LOC | Purpose |
|------|-----|---------|
| `GaplessIdGeneratorService.java` | 383 | Core ID generation with Hazelcast |
| `GaplessIdGeneratorServiceTest.java` | 232 | Tests |
| `GeneratedIdState.java` | 74 | Hazelcast queue/set state |
| `TimeoutMaxLeaseTimeLock.java` | 64 | Distributed lock |
| `HazelCastConfig.java` | 48 | Spring config |
| `BackgroundJobs.java` | 39 | Scheduled ID persistence |

### Dependencies Removed

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-spring</artifactId>
</dependency>
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-hibernate53</artifactId>
</dependency>
```

### Replacement Strategy

**Recommended: Database Sequence**

```java
@PrePersist
public void assignNetexId(IdentifiedEntity entity) {
    if (entity.getNetexId() == null) {
        String type = entity.getClass().getSimpleName();
        Long nextId = jdbcTemplate.queryForObject(
            "SELECT nextval('netex_id_seq')", Long.class);
        entity.setNetexId(PREFIX + ":" + type + ":" + nextId);
    }
}
```

- Stateless
- No distributed coordination
- ~10 LOC replaces ~1,270 LOC

**Details:** See [HAZELCAST_GAPLESSID_REMOVAL.md](./HAZELCAST_GAPLESSID_REMOVAL.md)

---

## Implementation Priority

### Recommended Order

1. **Hazelcast/GaplessID Removal** (do first)
   - Higher complexity reduction
   - Removes distributed state dependency
   - Makes testing simpler
   - Fewer files, cleaner removal

2. **Orika → MapStruct Migration** (do second)
   - Larger scope but lower risk
   - Can be done incrementally
   - Compile-time safety catches issues early

### Risk Comparison

| Factor | Orika → MapStruct | Hazelcast Removal |
|--------|-------------------|-------------------|
| Risk Level | Medium | Low-Medium |
| Rollback Difficulty | Easy (keep both temporarily) | Medium (DB migration) |
| Testing Coverage | Existing tests validate behavior | Need to verify ID uniqueness |
| Production Impact | None (same behavior) | Must not conflict with existing IDs |

---

## Combined Benefits

After both initiatives:

| Metric | Before | After |
|--------|--------|-------|
| External Dependencies | Orika + Hazelcast (2) | MapStruct (1, compile-time only) |
| Distributed State | Yes (Hazelcast) | No |
| Runtime Reflection | Yes (Orika) | No (MapStruct compile-time) |
| Multi-instance Coordination | Required | Not required |
| Total LOC Reduction | - | ~1,270 removed |
| Modernized LOC | - | ~5,665 rewritten |

---

## Quick Reference

### Files by Package

```
src/main/java/org/rutebanken/sobek/
├── config/
│   └── HazelCastConfig.java          [REMOVE]
├── lock/
│   ├── LockException.java            [REMOVE]
│   ├── MutateLock.java               [REMOVE]
│   └── TimeoutMaxLeaseTimeLock.java  [REMOVE]
├── netex/
│   ├── id/
│   │   ├── GaplessIdGeneratorService.java  [REMOVE]
│   │   ├── GeneratedIdState.java           [REMOVE]
│   │   ├── IdGeneratorException.java       [REMOVE]
│   │   ├── NetexIdAssigner.java            [REMOVE]
│   │   ├── NetexIdProvider.java            [REMOVE]
│   │   ├── NetexIdHelper.java              [KEEP - utility]
│   │   ├── TypeFromIdResolver.java         [KEEP - utility]
│   │   └── ValidPrefixList.java            [KEEP - config]
│   └── mapping/
│       ├── converter/                [REWRITE - 37 files]
│       ├── mapper/                   [REWRITE - 26 files]
│       └── NetexMapper.java          [REWRITE - orchestrator]
├── service/batch/
│   └── BackgroundJobs.java           [REMOVE]
└── ...
```

---

*Generated: January 2025*
