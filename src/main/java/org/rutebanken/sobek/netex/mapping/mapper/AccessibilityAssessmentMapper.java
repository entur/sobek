package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.AccessibilityAssessment;
import org.rutebanken.netex.model.LimitationStatusEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityAssessmentMapper extends CustomMapper<AccessibilityAssessment, org.rutebanken.sobek.model.AccessibilityAssessment> {

    @Autowired
    private NetexIdMapper netexIdMapper;

    @Override
    public void mapAtoB(AccessibilityAssessment accessibilityAssessment, org.rutebanken.sobek.model.AccessibilityAssessment accessibilityAssessment2, MappingContext context) {
        super.mapAtoB(accessibilityAssessment, accessibilityAssessment2, context);
        netexIdMapper.toSobekModel(accessibilityAssessment, accessibilityAssessment2);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.AccessibilityAssessment accessibilityAssessment, AccessibilityAssessment accessibilityAssessment2, MappingContext context) {
        super.mapBtoA(accessibilityAssessment, accessibilityAssessment2, context);


        if(accessibilityAssessment.getMobilityImpairedAccess() == null) {
            accessibilityAssessment2.setMobilityImpairedAccess(LimitationStatusEnumeration.UNKNOWN);
        }

        netexIdMapper.toNetexModel(accessibilityAssessment, accessibilityAssessment2);
    }

}
