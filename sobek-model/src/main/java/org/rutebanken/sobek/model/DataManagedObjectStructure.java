/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.util.*;


@MappedSuperclass
public abstract class DataManagedObjectStructure
        extends EntityInVersionStructure {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<KeyValue> keyValues = new ArrayList<KeyValue>();

    @Transient
    protected ExtensionsStructure extensions;

    @Transient
    protected String responsibilitySetRef;

    private String versionComment;

    private String changedBy;

    public ExtensionsStructure getExtensions() {
        return extensions;
    }

    public void setExtensions(ExtensionsStructure value) {
        this.extensions = value;
    }

    public String getResponsibilitySetRef() {
        return responsibilitySetRef;
    }

    public void setResponsibilitySetRef(String value) {
        this.responsibilitySetRef = value;
    }

    public List<KeyValue> getKeyValues() {
        return Collections.unmodifiableList(keyValues);
    }

    public void addKeyValue(KeyValue keyValue) {
        this.keyValues.add(keyValue);
    }

    public void addKeyValue(String key, String value) {
        this.keyValues.add(new KeyValue(key, value));
    }

    public void removeKeyValue(KeyValue keyValue) {
        this.keyValues.remove(keyValue);
    }

    public void clearKeyValues() {
        this.keyValues.clear();
    }

    public String getVersionComment() {
        return versionComment;
    }

    public void setVersionComment(String versionComment) {
        this.versionComment = versionComment;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    protected void mergeKeyValues(DataManagedObjectStructure existing) {
        if (existing.getKeyValues() != null && !existing.getKeyValues().isEmpty()) {
            // Copy keyValues from existing version
            for (KeyValue existingKv : existing.getKeyValues()) {
                // Check if this key already exists in the new version
                boolean keyExists = this.keyValues.stream()
                        .anyMatch(kv -> kv.getKey().equals(existingKv.getKey()));

                if (!keyExists) {
                    // Add the existing key-value pair
                    this.addKeyValue(
                            existingKv.getKey(),
                            existingKv.getValue()
                    );
                }
            }
        }
    }
}
