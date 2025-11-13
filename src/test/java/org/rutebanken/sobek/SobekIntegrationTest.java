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

package org.rutebanken.sobek;

import com.hazelcast.core.HazelcastInstance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.rutebanken.sobek.netex.id.GeneratedIdState;
import org.rutebanken.sobek.repository.TagRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.rutebanken.sobek.netex.id.GaplessIdGeneratorService.INITIAL_LAST_ID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
@ActiveProfiles({"test","gcs-blobstore"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public abstract class SobekIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(SobekIntegrationTest.class);

    @Autowired
    protected GeometryFactory geometryFactory;

    @Autowired
    protected HazelcastInstance hazelcastInstance;

    @Autowired
    protected GeneratedIdState generatedIdState;

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    protected VersionCreator versionCreator;

    @Value("${local.server.port}")
    protected int port;

    @Before
    @After
    public void clearRepositories() {
        clearIdGeneration();

        tagRepository.deleteAll();
        tagRepository.flush();
    }

    /**
     * Clear id_generator table and reset available ID queues and last IDs for entities.
     */
    private void clearIdGeneration() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        generatedIdState.getRegisteredEntityNames().forEach(entityName -> {
            hazelcastInstance.getQueue(entityName).clear();
            generatedIdState.setLastIdForEntity(entityName, INITIAL_LAST_ID);
            generatedIdState.getClaimedIdListForEntity(entityName).clear();
        });

        int updated = entityManager.createNativeQuery("DELETE FROM id_generator").executeUpdate();
        logger.debug("Cleared id generator table. deleted: {}", updated);
        transaction.commit();
        entityManager.close();
    }
}
