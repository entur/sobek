package org.rutebanken.sobek.rest.ParameterDto;

import org.rutebanken.sobek.model.job.ExportParams;

public class ExportParametersDto {

    public ExportParams toExportParams() {
        var ret = new ExportParams();
        return ret;
    }
}