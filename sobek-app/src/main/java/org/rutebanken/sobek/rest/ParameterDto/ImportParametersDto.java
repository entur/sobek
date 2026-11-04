package org.rutebanken.sobek.rest.ParameterDto;

import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;

public class ImportParametersDto {

    public boolean skipOutput = false;

    public VehicleTypeKindEnumeration vehicleTypeKind;

    public ImportParams toImportParams() {
        var ret = new ImportParams();
        ret.setSkipOutput(skipOutput);
        ret.setVehicleTypeKind(vehicleTypeKind);
        return ret;
    }
}