package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class PropulsionTypeListConverter implements AttributeConverter<List<PropulsionTypeEnumeration>, String> {

    @Override
    public String convertToDatabaseColumn(List<PropulsionTypeEnumeration> propulsionTypes) {
        if (propulsionTypes == null || propulsionTypes.isEmpty()) {
            return null;
        }
        return propulsionTypes.stream()
                .map(PropulsionTypeEnumeration::name)
                .collect(Collectors.joining(" "));
    }

    @Override
    public List<PropulsionTypeEnumeration> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(" "))
                .map(PropulsionTypeEnumeration::valueOf)
                .collect(Collectors.toList());
    }
}