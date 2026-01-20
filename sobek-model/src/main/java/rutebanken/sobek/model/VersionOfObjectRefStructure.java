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

import jakarta.persistence.MappedSuperclass;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@ToString
public class VersionOfObjectRefStructure implements Serializable {

    private String ref;

    private String version;

    public VersionOfObjectRefStructure() {
    }

    public VersionOfObjectRefStructure(EntityInVersionStructure entityInVersionStructure) {
        this.ref = entityInVersionStructure.getNetexId();
        this.version = String.valueOf(entityInVersionStructure.getVersion());
    }


    public VersionOfObjectRefStructure(String ref, String version) {
        this.ref = ref;
        this.version = version;
    }

    public VersionOfObjectRefStructure(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String value) {
        this.ref = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof VersionOfObjectRefStructure)) {
            return false;
        }

        VersionOfObjectRefStructure other = (VersionOfObjectRefStructure) object;

        return Objects.equals(this.ref, other.ref)
                && Objects.equals(this.version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, version);
    }

}
