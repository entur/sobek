package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class FuelTypeListConverter implements AttributeConverter<List<FuelTypeEnumeration>, String> {

    @Override
    public String convertToDatabaseColumn(List<FuelTypeEnumeration> fuelTypes) {
        if (fuelTypes == null || fuelTypes.isEmpty()) {
            return null;
        }
        return fuelTypes.stream()
                .map(FuelTypeEnumeration::name)
                .collect(Collectors.joining(" "));
    }

    @Override
    public List<FuelTypeEnumeration> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(" "))
                .map(FuelTypeEnumeration::valueOf)
                .collect(Collectors.toList());
    }
}