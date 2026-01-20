package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@MappedSuperclass
public abstract class StairEquipment_VersionStructure extends AccessEquipment_VersionStructure {
    private BigDecimal depth;
    private BigInteger numberOfSteps;
    private BigDecimal stepHeight;
    private BigDecimal stepLength;
    private Boolean stepColourContrast;
    private StepConditionEnumeration stepCondition;
    private HandrailEnumeration handrailType;
    private BigDecimal handrailHeight;
    private BigDecimal lowerHandrailHeight;
    private Boolean tactileWriting;
//    private StairRampEnumeration stairRamp;
//    private StairEndStructure topEnd;
//    private StairEndStructure bottomEnd;
}
