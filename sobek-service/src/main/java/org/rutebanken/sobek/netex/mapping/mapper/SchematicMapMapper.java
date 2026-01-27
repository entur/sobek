package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.SchematicMap;
import org.rutebanken.netex.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.SchematicMapMember;

import java.util.List;

public class SchematicMapMapper extends CustomMapper<SchematicMap, org.rutebanken.sobek.model.vehicle.SchematicMap> {

    @Override
    public void mapAtoB(SchematicMap netexSchematicMap, org.rutebanken.sobek.model.vehicle.SchematicMap sobekSchematicMap, MappingContext context) {
        super.mapAtoB(netexSchematicMap, sobekSchematicMap, context);
        if(netexSchematicMap.getMembers() != null &&
            netexSchematicMap.getMembers().getSchematicMapMember() != null &&
            !netexSchematicMap.getMembers().getSchematicMapMember().isEmpty()) {
            var rawMembers = netexSchematicMap.getMembers().getSchematicMapMember();
            List<SchematicMapMember> sobekMembers = mapperFacade.mapAsList(rawMembers, org.rutebanken.sobek.model.vehicle.SchematicMapMember.class, context);
            if (!sobekMembers.isEmpty()) {
                sobekSchematicMap.setMembers(sobekMembers);
            }
        }
        if(netexSchematicMap.getDepictedObjectRef() != null &&
                netexSchematicMap.getDepictedObjectRef().getRef() != null) {
            sobekSchematicMap.setDepictedObjectRef(netexSchematicMap.getDepictedObjectRef().getRef());
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.SchematicMap sobekSchematicMap, SchematicMap netexSchematicMap, MappingContext context) {
        super.mapBtoA(sobekSchematicMap, netexSchematicMap, context);
        if(sobekSchematicMap.getDepictedObjectRef() != null) {
            netexSchematicMap.setDepictedObjectRef(new VersionOfObjectRefStructure().withRef(sobekSchematicMap.getDepictedObjectRef()));
        }
    }
}
