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

package org.rutebanken.sobek.netex.id;

import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides generated NetexIDs for IdentifiedEntities when saved.
 * It uses the {@link IdGeneratorService} to retrieve the incremented number in the ID.
 * If the ID is explicity set, the valid prefix list is checked.
 * If the prefix matches it will try to use the claimed ID.
 */
@Component
public class NetexIdProvider {

    private static final Logger logger = LoggerFactory.getLogger(NetexIdProvider.class);

    private final IdGeneratorService idGenerator;

    private final ValidPrefixList validPrefixList;

    private final NetexIdHelper netexIdHelper;

    @Autowired
    public NetexIdProvider(IdGeneratorService idGenerator, ValidPrefixList validPrefixList, NetexIdHelper netexIdHelper) {
        this.idGenerator = idGenerator;
        this.validPrefixList = validPrefixList;
        this.netexIdHelper = netexIdHelper;
    }

    public String getGeneratedId(IdentifiedEntity identifiedEntity) {

        long longId = idGenerator.getNextIdForEntity(identifiedEntity.getClass());

        return netexIdHelper.getNetexId(identifiedEntity, longId);
    }

    public void claimId(IdentifiedEntity identifiedEntity) {

        String prefix = netexIdHelper.extractIdPrefix(identifiedEntity.getNetexId());

        if(validPrefixList.isValidPrefixForType(prefix, identifiedEntity.getClass())) {
            logger.debug("Claimed ID {} contains valid prefix for claiming: {}", identifiedEntity.getNetexId(), prefix);
            logger.trace("Accepting ID with prefix {}", prefix);
        }
    }
}
