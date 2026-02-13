package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Mapper;
import org.rutebanken.sobek.model.ValidBetween;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {ValidBetweenMapper.class})
public abstract class ValidBetweenListConverter {
    @Autowired
    protected ValidBetweenMapper validBetweenMapper;
    /**
     * Converts a list of ValidBetween to a single ValidBetween.
     * Takes the first element or returns null if list is empty.
     */
    public ValidBetween mapToSobek(List<org.rutebanken.netex.model.ValidBetween> validBetweenList) {
        if (validBetweenList == null || validBetweenList.isEmpty()) {
            return null;
        }
        return validBetweenMapper.mapToSobek(validBetweenList.getFirst());
    }

    /**
     * Converts a single ValidBetween to a list.
     */
    public List<org.rutebanken.netex.model.ValidBetween> mapToNetex(ValidBetween validBetween) {
        if (validBetween == null) {
            return null;
        }
        return List.of(validBetweenMapper.mapToNetex(validBetween));
    }
}