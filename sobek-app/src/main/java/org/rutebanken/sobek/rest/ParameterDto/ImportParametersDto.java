package org.rutebanken.sobek.rest.ParameterDto;

import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.importer.ImportType;

public class ImportParametersDto {

    public String importType = ImportType.MERGE.name();

    public boolean skipOutput = false;

    public ImportParams toImportParams() {
        var ret = new ImportParams();
        if(importType == null) {
            ret.setImportType(ImportType.MERGE);
        } else {
            ret.setImportType(ImportType.valueOf(this.importType));
        }
        ret.setSkipOutput(skipOutput);
        return ret;
    }
}