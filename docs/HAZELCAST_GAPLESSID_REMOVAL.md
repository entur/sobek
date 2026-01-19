# Hazelcast & GaplessID Removal Analysis

## Goal
Simplify the codebase by removing Hazelcast distributed state and the GaplessID generation logic, making endpoints stateless.

---

## Summary

| Category | Files | Lines of Code |
|----------|-------|---------------|
| **Files to REMOVE completely** | 14 | ~1,254 |
| **Files to MODIFY** | 10 | ~200 LOC changes |
| **Config/Resources to REMOVE** | 3 | ~15 |
| **Total removable LOC** | **17 files** | **~1,270 LOC** |

---

## Files to REMOVE Completely

### Main Code (10 files, 869 LOC)

| File | LOC | Purpose |
|------|-----|---------|
| `netex/id/GaplessIdGeneratorService.java` | 383 | Core ID generation with Hazelcast queues |
| `netex/id/GeneratedIdState.java` | 74 | Hazelcast queue/set state management |
| `netex/id/NetexIdProvider.java` | 83 | Interface between listener and generator |
| `netex/id/NetexIdAssigner.java` | 52 | Assigns IDs via NetexIdProvider |
| `netex/id/IdGeneratorException.java` | 29 | Custom exception |
| `config/HazelCastConfig.java` | 48 | Hazelcast Spring config |
| `lock/TimeoutMaxLeaseTimeLock.java` | 64 | Hazelcast distributed lock |
| `lock/MutateLock.java` | 55 | Lock interface |
| `lock/LockException.java` | 42 | Lock exception |
| `service/batch/BackgroundJobs.java` | 39 | Scheduled job for persisting claimed IDs |

### Test Code (4 files, 380 LOC)

| File | LOC | Purpose |
|------|-----|---------|
| `netex/id/GaplessIdGeneratorServiceTest.java` | 232 | Tests for GaplessID |
| `lock/TimeoutMaxLeaseTimeLockTest.java` | 110 | Tests for distributed lock |
| `netex/id/RandomizedTestNetexIdGenerator.java` | 38 | Test helper |

### Config/Resources (3 files, ~15 LOC)

| File | LOC | Purpose |
|------|-----|---------|
| `hazelcast.xml` | 7 | Hazelcast XML config |
| Database migration for `id_generator` table | ~8 | Schema for ID tracking |

---

## Files to MODIFY

### Must Change (ID Strategy Replacement)

| File | Current LOC | Changes Needed |
|------|-------------|----------------|
| `repository/listener/IdentifiedEntityListener.java` | 30 | Replace `NetexIdAssigner` with simpler strategy (UUID or DB sequence) |
| `config/ApplicationContextProvider.java` | ~50 | Remove `getNetexIdAssigner()` method |
| `model/identification/IdentifiedEntity.java` | ~30 | Remove `@EntityListeners` or change listener |

### Must Change (Remove Hazelcast References)

| File | Changes Needed |
|------|----------------|
| `SobekIntegrationTest.java` | Remove Hazelcast queue clearing in `clearIdGeneration()` |
| `importer/handler/VehicleImportHandler.java` | Remove `HazelcastInstance` injection |
| `application-local.properties` | Remove hazelcast config lines (~8 lines) |
| `pom.xml` | Remove hazelcast dependencies (2 dependencies) |

### Keep But Simplify

| File | LOC | Keep/Modify |
|------|-----|-------------|
| `netex/id/NetexIdHelper.java` | 110 | **KEEP** - ID parsing/formatting utilities |
| `netex/id/ValidPrefixList.java` | 86 | **KEEP** - Config for valid prefixes |
| `netex/id/TypeFromIdResolver.java` | 46 | **KEEP** - Type resolution from ID |
| `netex/id/NetexIdHelperTest.java` | 59 | **KEEP** - Tests for helper |

---

## What the GaplessID System Does (For Context)

```
┌──────────────────┐     ┌────────────────────┐     ┌─────────────────┐
│ Entity @PrePersist│────>│ IdentifiedEntity   │────>│ NetexIdAssigner │
│ (Hibernate)       │     │ Listener           │     │                 │
└──────────────────┘     └────────────────────┘     └────────┬────────┘
                                                              │
                         ┌────────────────────┐               │
                         │ NetexIdProvider    │<──────────────┘
                         └────────┬───────────┘
                                  │
                         ┌────────▼───────────┐
                         │ GaplessIdGenerator │
                         │ Service            │
                         └────────┬───────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼───────┐        ┌───────▼───────┐        ┌───────▼───────┐
│ Hazelcast     │        │ Hazelcast     │        │ PostgreSQL    │
│ IQueue        │        │ ISet          │        │ id_generator  │
│ (available)   │        │ (claimed)     │        │ table         │
└───────────────┘        └───────────────┘        └───────────────┘
```

**Why it exists:** Legacy systems required short, gapless numeric IDs (e.g., `NSR:Vehicle:42`).

**Why remove it:**
- Adds significant complexity
- Requires Hazelcast for multi-instance coordination
- Makes the system stateful
- UUID or DB sequence would be simpler

---

## Replacement Strategy Options

### Option 1: UUID-based IDs (Simplest)
```java
@PrePersist
public void assignNetexId(IdentifiedEntity entity) {
    if (entity.getNetexId() == null) {
        String type = entity.getClass().getSimpleName();
        entity.setNetexId(PREFIX + ":" + type + ":" + UUID.randomUUID());
    }
}
```
- **Pros:** Stateless, no coordination needed
- **Cons:** Long IDs, not human-readable

### Option 2: Database Sequence (Recommended)
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
- **Pros:** Short IDs, sequential, no distributed state
- **Cons:** Single point of contention (acceptable for most workloads)

### Option 3: Snowflake/ULID
- **Pros:** Sortable, distributed, no coordination
- **Cons:** Longer than sequential IDs

---

## Database Migration Required

### Remove
```sql
-- Drop the id_generator table
DROP TABLE IF EXISTS id_generator;
```

### Add (if using DB sequence)
```sql
-- Create a simple sequence
CREATE SEQUENCE IF NOT EXISTS netex_id_seq START WITH 1 INCREMENT BY 1;
```

---

## Dependencies to Remove from pom.xml

```xml
<!-- REMOVE -->
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-spring</artifactId>
    <version>${hazelcast.version}</version>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-hibernate53</artifactId>
    <version>${hazelcast-hibernate53.version}</version>
</dependency>
```

Also remove from surefire plugin:
```xml
-Dhazelcast.ignoreXxeProtectionFailures=true
```

---

## Properties to Remove from application-local.properties

```properties
# REMOVE these lines
spring.jpa.properties.hibernate.cache.region.factory_class=org.rutebanken.sobek.hazelcast.SobekHazelcastCacheRegionFactory
hazelcast.performance.monitoring.enabled=true
hazelcast.performance.monitoring.delay.seconds=2
```

Note: Second-level cache is already disabled (`use_second_level_cache=false`), so removing Hazelcast Hibernate integration has minimal impact.

---

## Migration Steps

### Phase 1: Add New ID Strategy
1. Create new `SimpleNetexIdAssigner` using DB sequence or UUID
2. Add database migration for sequence (if needed)
3. Update `IdentifiedEntityListener` to use new assigner
4. Run tests to verify ID generation still works

### Phase 2: Remove Hazelcast Dependencies
1. Remove Hazelcast from `pom.xml`
2. Delete `HazelCastConfig.java`
3. Delete `hazelcast.xml`
4. Update `application-local.properties`

### Phase 3: Remove GaplessID Code
1. Delete all files listed in "Files to REMOVE Completely"
2. Remove `id_generator` table migration (or add DROP migration)
3. Update `SobekIntegrationTest` to remove Hazelcast cleanup
4. Remove `HazelcastInstance` from `VehicleImportHandler`

### Phase 4: Cleanup
1. Remove unused imports across codebase
2. Update CLAUDE.md documentation
3. Run full test suite

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| Existing IDs in production | New strategy must not conflict with existing IDs |
| Multi-instance ID collisions | DB sequence handles this; UUID is inherently unique |
| Performance regression | DB sequence is fast; benchmark if concerned |
| Breaking external integrations | ID format (`NSR:Type:value`) remains unchanged |

---

## Summary Metrics

| Metric | Value |
|--------|-------|
| **Files removed** | 17 |
| **LOC removed** | ~1,270 |
| **Dependencies removed** | 2 (hazelcast-spring, hazelcast-hibernate53) |
| **Complexity reduction** | Significant (removes distributed state coordination) |
| **Files modified** | 10 |
| **New files needed** | 1-2 (new ID assigner, migration) |

---

*Generated: January 2025*
