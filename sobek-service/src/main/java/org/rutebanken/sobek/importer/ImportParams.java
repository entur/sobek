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

package org.rutebanken.sobek.importer;

import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;

@Getter
@Setter
public class ImportParams {

    private boolean skipOutput = false;

    /**
     * The kind of the vehicle types in the import whose id does not say it: NeTEx has no field for the kind, so only
     * an id in the contractual codespace (CVT) is contractual by itself. Ids in other codespaces get this kind.
     */
    private VehicleTypeKindEnumeration vehicleTypeKind;
}
