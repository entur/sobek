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

public class VersionFrame_VersionStructure
        extends DataManagedObjectStructure {

    protected EmbeddableMultilingualString name;
    protected EmbeddableMultilingualString description;
    protected TypeOfFrameRefStructure typeOfFrameRef;
    protected VersionRefStructure baselineVersionFrameRef;
    protected VersionFrameDefaultsStructure frameDefaults;
    protected Versions_RelStructure versions;

    public EmbeddableMultilingualString getName() {
        return name;
    }

    public void setName(EmbeddableMultilingualString value) {
        this.name = value;
    }

    public EmbeddableMultilingualString getDescription() {
        return description;
    }

    public void setDescription(EmbeddableMultilingualString value) {
        this.description = value;
    }

    public TypeOfFrameRefStructure getTypeOfFrameRef() {
        return typeOfFrameRef;
    }

    public void setTypeOfFrameRef(TypeOfFrameRefStructure value) {
        this.typeOfFrameRef = value;
    }

    public VersionRefStructure getBaselineVersionFrameRef() {
        return baselineVersionFrameRef;
    }

    public void setBaselineVersionFrameRef(VersionRefStructure value) {
        this.baselineVersionFrameRef = value;
    }

    public VersionFrameDefaultsStructure getFrameDefaults() {
        return frameDefaults;
    }

    public void setFrameDefaults(VersionFrameDefaultsStructure value) {
        this.frameDefaults = value;
    }

    public Versions_RelStructure getVersions() {
        return versions;
    }

    public void setVersions(Versions_RelStructure value) {
        this.versions = value;
    }

}
