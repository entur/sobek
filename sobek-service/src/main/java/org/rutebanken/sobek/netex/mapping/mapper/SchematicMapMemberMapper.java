package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.SchematicMapMember_VersionedChildStructure;

public class SchematicMapMemberMapper extends CustomMapper<SchematicMapMember_VersionedChildStructure, org.rutebanken.sobek.model.vehicle.SchematicMapMember> {

    @Override
    public void mapAtoB(SchematicMapMember_VersionedChildStructure netexSchematicMapMember, org.rutebanken.sobek.model.vehicle.SchematicMapMember sobekSchematicMapMember, MappingContext context) {
        super.mapAtoB(netexSchematicMapMember, sobekSchematicMapMember, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.SchematicMapMember sobekSchematicMapMember, SchematicMapMember_VersionedChildStructure netexSchematicMapMember, MappingContext context) {
        super.mapBtoA(sobekSchematicMapMember, netexSchematicMapMember, context);
    }
}
