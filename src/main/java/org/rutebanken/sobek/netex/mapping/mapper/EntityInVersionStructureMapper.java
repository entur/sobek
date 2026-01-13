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

package org.rutebanken.sobek.netex.mapping.mapper;

import com.google.common.primitives.Longs;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.EntityInVersionStructure;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityInVersionStructureMapper extends CustomMapper<EntityInVersionStructure, org.rutebanken.sobek.model.EntityInVersionStructure> {

    private final NetexIdMapper netexIdMapper;

    @Autowired
    public EntityInVersionStructureMapper(NetexIdMapper netexIdMapper) {
        this.netexIdMapper = netexIdMapper;
    }

    @Override
    public void mapAtoB(EntityInVersionStructure netexEntity, org.rutebanken.sobek.model.EntityInVersionStructure sobekEntity, MappingContext context) {
        netexIdMapper.toSobekModel(netexEntity, sobekEntity);

        // Version is a field of superclass EntityInVersionStructure. Should strictly be mapped in an EntityInVersionStructureMapper or Converter.

        if (netexEntity.getVersion() != null) {
            if (netexEntity.getVersion().equals("any")) {
                sobekEntity.setVersion(-1L); // Need to handle this value in import.
            } else {
                Long longVersion = Longs.tryParse(netexEntity.getVersion());
                if (longVersion != null) {
                    sobekEntity.setVersion(longVersion);
                } else {
                    throw new NetexMappingException("Received version in netex format. " +
                            "But cannot parse version. Expecting a long value or the String 'any'. " +
                            "Value is: " + netexEntity.getVersion() + " Object: " + netexEntity);
                }
            }
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.EntityInVersionStructure sobekEntity, EntityInVersionStructure netexEntity, MappingContext context) {
        netexIdMapper.toNetexModel(sobekEntity, netexEntity);
        netexEntity.setVersion(String.valueOf(sobekEntity.getVersion()));
    }
}

