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

package org.rutebanken.sobek.repository.listener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import org.rutebanken.sobek.general.JPAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service generates a new NeTEx ID for a given entity.
 */
@Service
public class IdGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(IdGeneratorService.class);

    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public IdGeneratorService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Get a new ID for a certain entity.
     *
     * @param entityType Type of the entity, for instance Vehicle.class
     * @return the generated long value
     */
    public long getNextIdForEntity(Class<?> entityType) {
        logger.debug("Will fetch new ID from database sequence for {}", entityType.getSimpleName());

        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            String entityTypeName = JPAUtils.getTableName(em, entityType);

            String sql = "SELECT nextval('netex_" + entityTypeName + "_seq')";

            Query sqlQuery = em.createNativeQuery(sql);

            return Long.parseLong(sqlQuery.getSingleResult().toString());
        }
    }


}
