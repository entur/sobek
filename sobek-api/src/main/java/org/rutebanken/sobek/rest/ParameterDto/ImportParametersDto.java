package org.rutebanken.sobek.rest.ParameterDto;

import jakarta.ws.rs.QueryParam;
import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.importer.ImportType;

public class ImportParametersDto {

    @QueryParam(value = "importType")
    public String importType = ImportType.MERGE.name();

    @QueryParam(value = "skipOutput")
    public boolean skipOutput = false;

    public ImportParams toImportParams() {
        var ret = new ImportParams();
        ret.setImportType(ImportType.valueOf(this.importType));
        ret.setSkipOutput(skipOutput);
        return ret;
    }
}