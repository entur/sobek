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

package org.rutebanken.sobek.changelog;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.StringWriter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityChangedEvent {

    public enum EntityType {VEHICLE}

    public enum CrudAction {CREATE, UPDATE, REMOVE, DELETE}

    public String msgId;

    public EntityType entityType;

    public String entityId;

    public Long entityVersion;

    public Long entityChanged;

    public CrudAction crudAction;

    public String toString() {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, this);
        return writer.toString();
    }
}
