package org.rutebanken.sobek.rest.graphql;

import org.hibernate.Hibernate;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.TrainElementTypeEnumeration;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import java.util.List;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.rutebanken.sobek.rest.graphql.fetchers.*;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * GraphQL Query Controller for Sobek Vehicle Register
 */
@Controller
public class QueryController {

    private final VehicleFetcher vehicleFetcher;
    private final FleetFetcher fleetFetcher;
    private final VehicleTypeFetcher vehicleTypeFetcher;
    private final DeckPlanFetcher deckPlanFetcher;
    private final OrganisationFetcher organisationFetcher;
    private final CodeValueFetcher codeValueFetcher;
    private final VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher;
    private final AuthorizationService authorizationService;

    public QueryController(
        VehicleFetcher vehicleFetcher,
        FleetFetcher fleetFetcher,
        VehicleTypeFetcher vehicleTypeFetcher,
        DeckPlanFetcher deckPlanFetcher,
        OrganisationFetcher organisationFetcher, CodeValueFetcher codeValueFetcher,
        VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher,
        AuthorizationService authorizationService) {
        this.vehicleFetcher = vehicleFetcher;
        this.fleetFetcher = fleetFetcher;
        this.vehicleTypeFetcher = vehicleTypeFetcher;
        this.deckPlanFetcher = deckPlanFetcher;
        this.organisationFetcher = organisationFetcher;
        this.codeValueFetcher = codeValueFetcher;
        this.vehicleTypeDeckPlanFetcher = vehicleTypeDeckPlanFetcher;
        this.authorizationService = authorizationService;
    }

    // Query mappings
    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object vehicles(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size) throws Exception {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return vehicleFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object fleets(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size) {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return fleetFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object vehicleTypes(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size)  {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return vehicleTypeFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object deckPlans(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size) {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return deckPlanFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object organisations(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size)  {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return organisationFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object codeValues(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size)  {
        graphql.schema.DataFetchingEnvironment env = createEnvironment(filter, page, size);
        return codeValueFetcher.get(env);
    }

    @SchemaMapping(typeName = "VehicleType", field = "deckPlan")
    public Object vehicleTypeDeckPlan(Object source)  {
        graphql.schema.DataFetchingEnvironment env = createFieldEnvironment(source);
        return vehicleTypeDeckPlanFetcher.get(env);
    }

    // The fields of trains and compound trains. The source can be a lazy proxy, whose class does not say what it is.
    @SchemaMapping(typeName = "VehicleType", field = "subtype")
    public VehicleTypeSubtype vehicleTypeSubtype(VehicleType source) {
        return VehicleTypeSubtype.of(source);
    }

    @SchemaMapping(typeName = "VehicleType", field = "numberOfCars")
    public Integer vehicleTypeNumberOfCars(VehicleType source) {
        return Hibernate.unproxy(source) instanceof Train train ? train.getNumberOfCars() : null;
    }

    @SchemaMapping(typeName = "VehicleType", field = "trainSizeType")
    public TrainSizeEnumeration vehicleTypeTrainSizeType(VehicleType source) {
        return Hibernate.unproxy(source) instanceof Train train ? train.getTrainSizeType() : null;
    }

    @SchemaMapping(typeName = "VehicleType", field = "trainElementType")
    public TrainElementTypeEnumeration vehicleTypeTrainElementType(VehicleType source) {
        return Hibernate.unproxy(source) instanceof TrainElement element ? element.getTrainElementType() : null;
    }

    @SchemaMapping(typeName = "VehicleType", field = "trainComponents")
    public List<TrainComponent> vehicleTypeTrainComponents(VehicleType source) {
        return Hibernate.unproxy(source) instanceof Train train ? train.getComponents() : null;
    }

    @SchemaMapping(typeName = "VehicleType", field = "compoundTrainComponents")
    public List<TrainInCompoundTrain> vehicleTypeCompoundTrainComponents(VehicleType source) {
        return Hibernate.unproxy(source) instanceof CompoundTrain compoundTrain ? compoundTrain.getComponents() : null;
    }

    @SchemaMapping(typeName = "VehicleType", field = "changedBy")
    public String vehicleTypeChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for Vehicle
    @SchemaMapping(typeName = "Vehicle", field = "changedBy")
    public String vehicleChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for VehicleTypeVehicle
    @SchemaMapping(typeName = "VehicleTypeVehicle", field = "changedBy")
    public String vehicleTypeVehicleChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for Fleet
    @SchemaMapping(typeName = "Fleet", field = "changedBy")
    public String fleetChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for DeckPlan
    @SchemaMapping(typeName = "DeckPlan", field = "changedBy")
    public String deckPlanChangedBy(Object source) {
        return getChangedBy(source);
    }

    private String getChangedBy(Object source) {
        if (source instanceof DataManagedObjectStructure dmo && !authorizationService.isGuest()) {
            return dmo.getChangedBy();
        }
        return null;
    }

    private graphql.schema.DataFetchingEnvironment createEnvironment(
            Map<String, Object> filter,
            Integer page,
            Integer size) {
        Map<String, Object> arguments = new java.util.HashMap<>();
        if (filter != null) {
            arguments.put("filter", filter);
        }
        if (page != null) {
            arguments.put("page", page);
        }
        if (size != null) {
            arguments.put("size", size);
        }

        return graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .arguments(arguments)
                .build();
    }

    private graphql.schema.DataFetchingEnvironment createFieldEnvironment(Object source) {
        return graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .source(source)
                .build();
    }
}