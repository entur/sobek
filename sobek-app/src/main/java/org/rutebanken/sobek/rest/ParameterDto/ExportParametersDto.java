package org.rutebanken.sobek.rest.ParameterDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rutebanken.sobek.model.job.ExportParams;
import org.rutebanken.sobek.model.job.VehicleTypeKindSelection;

@ToString
@Getter
@Setter
public class ExportParametersDto {

    /**
     * Optional. Restricts the export to objects owned by this organisation, e.g. "ATU".
     * Administrators may omit it, which means "all dataowners".
     */
    private String dataOwner;

    /**
     * Required by the full export. PHYSICAL, CONTRACTUAL or BOTH: which kinds of vehicle types to export.
     * Vehicles are only exported when PHYSICAL or BOTH is requested.
     */
    private VehicleTypeKindSelection vehicleTypeKind;

    public ExportParams toExportParams() {
        var ret = new ExportParams();
        ret.setVehicleTypeKind(vehicleTypeKind);
        if(dataOwner != null) {
            ret.setDataOwner(dataOwner);
        }
        return ret;
    }
}