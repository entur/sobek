# Sobek data model

A simplified view of the core entities in Sobek. It leaves out detail that is not needed to understand how the entities relate.

## Entity-relationship diagrams

### Contractual data space

```mermaid
erDiagram
    SERVICE_JOURNEY }o--o{ CONTRACTUAL_VEHICLE_TYPE: "serviced by"
```

### Vehicles - realtime data

```mermaid
erDiagram
SIRI-VM-monitoredVehicleJourney }o--o{ VEHICLE: "vehicleRef"
SIRI-ET }o--o{ VEHICLE: "vehicleRef"

```

### Deck plan

```mermaid
erDiagram
    DECK_PLAN ||--o{ DECK_LEVEL : has
    DECK_PLAN ||--o{ DECK : has
    DECK }o--o| DECK_LEVEL : "placed on"
    DECK ||--o{ PASSENGER_SPACE : has
    DECK ||--o{ SPOT_ROW : has
    DECK ||--o{ SPOT_COLUMN : has
    PASSENGER_SPACE ||--o{ PASSENGER_SPACE : "contains"
    PASSENGER_SPACE ||--o{ PASSENGER_ENTRANCE : has
    PASSENGER_SPACE ||--o{ DECK_SPACE_CAPACITY : has
    PASSENGER_SPACE ||--o{ PASSENGER_SPOT : has
    PASSENGER_SPACE ||--o{ LUGGAGE_SPOT : has
    PASSENGER_SPACE ||--o{ SPOT_AFFINITY : has
    PASSENGER_SPOT }o--o| SPOT_ROW : "in"
    PASSENGER_SPOT }o--o| SPOT_COLUMN : "in"
    LUGGAGE_SPOT }o--o| SPOT_ROW : "in"
    LUGGAGE_SPOT }o--o| SPOT_COLUMN : "in"
    SPOT_AFFINITY }o--o{ PASSENGER_SPOT : relates
    SPOT_AFFINITY }o--o{ LUGGAGE_SPOT : relates
    
    
```
## Simplifications

- **`CONTRACTUAL_VEHICLE_TYPE`** 
- **`DECK_PLAN`** stands for the whole deck plan tree: `DeckLevel`, `Deck`, `DeckSpace`, `PassengerSpace`, `PassengerSpot`, `SpotRow`, `SpotColumn`, `LocatableSpot`, `SchematicMap` and the equipment placed on them.
- **Left out entirely:** `Equipment` and `VehicleEquipmentProfile`, the `KeyValue`, `CodeValue` and `AlternativeName` helper entities, and `ExportJob`.
- Only a subset of the attributes is listed for each entity.
- Every entity is versioned. The key is `id` plus `version`, and the relationships are drawn between entities rather than between specific versions.
- `dataOwnerRef` marks the data owner and is used for authorisation.
