package org.rutebanken.sobek.rest.ParameterDto;

import org.rutebanken.sobek.importer.ImportParams;

public class ImportParametersDto {

    public boolean skipOutput = false;

    public ImportParams toImportParams() {
        var ret = new ImportParams();
        ret.setSkipOutput(skipOutput);
        return ret;
    }
}