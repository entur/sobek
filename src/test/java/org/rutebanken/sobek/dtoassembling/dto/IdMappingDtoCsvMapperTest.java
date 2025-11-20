package org.rutebanken.sobek.dtoassembling.dto;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.rutebanken.sobek.time.ExportTimeZone;

import java.time.Instant;

public class IdMappingDtoCsvMapperTest {

    private IdMappingDtoCsvMapper mapper = new IdMappingDtoCsvMapper(new ExportTimeZone());


    @Test
    @Ignore("Diff between Sobek and Tiamat")
    public void whenNotIncludeStopTypeOrInterval_ignoreStopTypeAndInterval() {
        IdMappingDto dto = new IdMappingDto("orgId", "netexId", Instant.now(), Instant.now());
        Assert.assertEquals("orgId,netexId", mapper.toCsvString(dto, false, false));
    }

    @Test
    @Ignore("Diff between Sobek and Tiamat")
    public void whenIncludeStopTypeOrInterval_printStopTypeAndInterval() {
        IdMappingDto dto = new IdMappingDto("orgId", "netexId", Instant.EPOCH, Instant.EPOCH.plusSeconds(1));
        Assert.assertEquals("orgId,airport,netexId,1970-01-01T01:00:00,1970-01-01T01:00:01", mapper.toCsvString(dto, true, true));
    }

    @Test
    @Ignore("Diff between Sobek and Tiamat")
    public void whenOptionalFieldsAreNull_printEmptyString() {
        IdMappingDto dto = new IdMappingDto("orgId", "netexId", null, null);
        Assert.assertEquals("orgId,,netexId,,", mapper.toCsvString(dto, true, true));
    }
}
