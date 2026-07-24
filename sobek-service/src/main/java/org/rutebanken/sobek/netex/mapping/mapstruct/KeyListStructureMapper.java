package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.KeyValue;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = SobekMapperConfig.class)
public interface KeyListStructureMapper {
    @Named("mapKeyListToSobek")
    default List<KeyValue> mapToSobek(
            KeyListStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getKeyValue() == null) {
            return new ArrayList<>();
        }

        return source.getKeyValue().stream()
                .map(this::mapKeyValueStructureToKeyValue)
                .collect(Collectors.toList());
    }

    default KeyValue mapKeyValueStructureToKeyValue(KeyValueStructure netexKeyValue) {
        if (netexKeyValue == null) {
            return null;
        }

        KeyValue keyValue = new KeyValue();
        keyValue.setKey(netexKeyValue.getKey());
        keyValue.setValue(netexKeyValue.getValue());
        return keyValue;
    }

    @Named("mapKeyListToNetex")
    default KeyListStructure mapToNetex(
            List<KeyValue> source,
            @Context MappingContext context
    ) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        KeyListStructure keyList = new KeyListStructure();

        for (KeyValue kv : source) {
            KeyValueStructure netexKeyValue = new KeyValueStructure();
            netexKeyValue.setKey(kv.getKey());
            netexKeyValue.setValue(kv.getValue());
            keyList.getKeyValue().add(netexKeyValue);
        }

        return keyList;
    }

}


