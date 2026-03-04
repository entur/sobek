package org.rutebanken.sobek.rest.ParameterDto;

import lombok.ToString;
import org.rutebanken.sobek.model.job.ExportParams;

@ToString
public class ExportParametersDto {

    public ExportParams toExportParams() {
        var ret = new ExportParams();
        return ret;
    }
}