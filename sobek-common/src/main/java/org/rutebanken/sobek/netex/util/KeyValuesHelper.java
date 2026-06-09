package org.rutebanken.sobek.netex.util;

import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeyValuesHelper {

    public static void SetToKeyValues(DataManagedObjectStructure objectWithKeyValues, String propertyName, String value) {
        if(objectWithKeyValues.getKeyList() == null) {
            objectWithKeyValues.setKeyList(new KeyListStructure());
        }

        Optional<KeyValueStructure> existing = objectWithKeyValues.getKeyList().getKeyValue().stream().filter(kv -> kv.getKey().equals(propertyName)).findFirst();
        // A keyvalue with value == null is not allowed according to the XSD, so we remove it if it exists.
        if(value == null) {
            existing.ifPresent(objectWithKeyValues.getKeyList().getKeyValue()::remove);
            return;
        }
        existing.ifPresentOrElse(keyValueStructure -> keyValueStructure.setValue(value), () -> objectWithKeyValues.getKeyList().getKeyValue().add(new KeyValueStructure().withKey(propertyName).withValue(value)));
    }

    public static <PropertyType> void SetFromKeyValues(String propertyName, KeyListStructure keyList, Function<String, PropertyType> convertFunction, Consumer<PropertyType> setFunction) {
        if(keyList == null) {
            setFunction.accept(null);
            return;
        }

        var value = keyList.getKeyValue().stream().filter(kv -> kv.getKey().equals(propertyName)).findFirst();
        if(!value.isPresent()) {
            setFunction.accept(null);
            return;
        }

        if(value.get().getValue() == null) {
            setFunction.accept(null);
            return;
        }

        setFunction.accept(convertFunction.apply(value.get().getValue()));
    }
}
