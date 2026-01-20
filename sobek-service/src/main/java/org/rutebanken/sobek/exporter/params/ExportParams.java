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

package org.rutebanken.sobek.exporter.params;

import com.google.common.base.MoreObjects;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.ws.rs.QueryParam;

import java.util.Arrays;
import java.util.List;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.COUNTRY_REF_ARG_DESCRIPTION;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.COUNTY_REF_ARG_DESCRIPTION;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.MUNICIPALITY_REF_ARG_DESCRIPTION;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.SEARCH_WITH_CODE_SPACE_ARG_DESCRIPTION;

/**
 * Export parameters.
 * Parameters specific for search related to a certain type like StopPlace does not necessary belong here.
 */
@Schema(description = "Export parameters")
public class ExportParams {

    public enum ExportMode {NONE, RELEVANT, ALL}

    public enum VersionValidity {ALL, CURRENT, CURRENT_FUTURE, MAX_VERSION}

    public static final ExportMode DEFAULT_SERVICE_FRAME_EXPORT_MODE = ExportMode.NONE;

    @QueryParam(value = "municipalityReference")
    @Parameter(description = MUNICIPALITY_REF_ARG_DESCRIPTION)
    @Schema(description = "municipalityReference")
    private List<String> municipalityReferences;

    @QueryParam(value = "countyReference")
    @Parameter(description = COUNTY_REF_ARG_DESCRIPTION)
    @Schema(description = "countyReference")
    private List<String> countyReferences;

    @QueryParam(value = "countryReference")
    @Parameter(description = COUNTRY_REF_ARG_DESCRIPTION)
    @Schema(description = "countryReference")
    private List<String> countryReferences;

    @QueryParam(value = "codeSpace")
    @Parameter(description = SEARCH_WITH_CODE_SPACE_ARG_DESCRIPTION)
    @Schema(description = "codeSpace")
    private String codeSpace;

    private ExportParams(List<String> municipalityReferences,
                         List<String> countyReferences,
                         List<String> countryReferences,
                         String codeSpace) {
        this.municipalityReferences = municipalityReferences;
        this.countyReferences = countyReferences;
        this.countryReferences = countryReferences;
        this.codeSpace = codeSpace;
    }

    public ExportParams() {
    }

    public List<String> getMunicipalityReferences() {
        return municipalityReferences;
    }

    public List<String> getCountyReferences() {
        return countyReferences;
    }
    public List<String> getCountryReferences() {
        return countryReferences;
    }
    public String getCodeSpace() {
        return codeSpace;
    }

    public static ExportParams.Builder newExportParamsBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("municipalityReferences", municipalityReferences)
                .add("countyReferences", countyReferences)
                .add("countryReferences", countryReferences)
                .add("codeSpace", codeSpace)
                .toString();
    }

    public static class Builder {
        private List<String> municipalityReferences;
        private List<String> countyReferences;
        private List<String> countryReferences;
        private String codeSpace;

        private Builder() {
        }

        public Builder setMunicipalityReferences(List<String> municipalityReferences) {
            this.municipalityReferences = municipalityReferences;
            return this;
        }

        public Builder setMunicipalityReference(String... municipalityReference) {
            this.municipalityReferences = Arrays.asList(municipalityReference);
            return this;
        }

        public Builder setCountyReferences(List<String> countyReferences) {
            this.countyReferences = countyReferences;
            return this;
        }

        public Builder setCountyReference(String... countyReference) {
            this.countyReferences = Arrays.asList(countyReference);
            return this;
        }

        public Builder setCountryReferences(List<String> countryReferences) {
            this.countryReferences = countryReferences;
            return this;
        }

        public Builder setCountryReference(String... countryReference) {
            this.countryReferences = Arrays.asList(countryReference);
            return this;
        }

        public Builder setCodeSpace(String codeSpace) {
            this.codeSpace = codeSpace;
            return this;
        }

        public ExportParams build() {
            return new ExportParams(municipalityReferences,
                    countyReferences,
                    countryReferences,
                    codeSpace);
        }
    }
}
