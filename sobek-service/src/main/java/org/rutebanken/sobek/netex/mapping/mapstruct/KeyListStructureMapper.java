package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.Value;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.HashMap;
import java.util.Map;

@Mapper()
public interface KeyListStructureMapper {
    default Map<String, Value> mapToSobek(
            org.rutebanken.netex.model.KeyListStructure source,
            @Context MappingContext context
    ) {
        if(source != null && source.getKeyValue() != null && !source.getKeyValue().isEmpty()) {
            HashMap<String, Value> stringValueMap = new HashMap<>();
            for(KeyValueStructure keyValueStructure : source.getKeyValue()) {
                stringValueMap.put(keyValueStructure.getKey(), new Value(keyValueStructure.getValue()));
            }
            return stringValueMap;
        }
        return null;
    };

    default KeyListStructure mapToNetex(
            Map<String, Value> source,
            @Context MappingContext context
    ) {
        if(source != null) {
            KeyListStructure keyListStructure = new KeyListStructure();
            for (String key : source.keySet()) {
                Value values = source.get(key);
                if(values != null && values.getItems() != null) {
                    String value = String.join(",", values.getItems());
                    keyListStructure.getKeyValue().add(new KeyValueStructure().withKey(key).withValue(value));
                } else {
                    // No values
                    keyListStructure.getKeyValue().add(new KeyValueStructure().withKey(key));
                }
            }
            if(keyListStructure.getKeyValue().isEmpty()) {
                return null;
            }
            return keyListStructure;
        }
        return null;
    };

}


