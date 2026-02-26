package org.rutebanken.sobek.netex.util;

import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;

import java.util.Optional;

public class KeyValuesHelper {

    public static void AddToKeyValues(DataManagedObjectStructure objectWithKeyValues, String uniqueKey, String value) {
        if(objectWithKeyValues.getKeyList() == null) {
            objectWithKeyValues.setKeyList(new KeyListStructure());
        }

        Optional<KeyValueStructure> existing = objectWithKeyValues.getKeyList().getKeyValue().stream().filter(kv -> kv.getKey().equals(uniqueKey)).findFirst();
        existing.ifPresentOrElse(keyValueStructure -> keyValueStructure.setValue(value), () -> objectWithKeyValues.getKeyList().getKeyValue().add(new KeyValueStructure().withKey(uniqueKey).withValue(value)));
    }
}
