package org.rutebanken.sobek.rest.ParameterDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rutebanken.sobek.model.job.ExportParams;

@ToString
@Getter
@Setter
public class ExportParametersDto {

    /**
     * Optional. Restricts the export to objects owned by this organisation, e.g. "ATU".
     * Administrators may omit it, which means "all dataowners".
     */
    private String dataOwner;

    public ExportParams toExportParams() {
        var ret = new ExportParams();
        if(dataOwner != null) {
            ret.setDataOwner(dataOwner);
        }
        return ret;
    }
}