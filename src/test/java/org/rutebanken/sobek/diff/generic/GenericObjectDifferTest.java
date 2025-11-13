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

package org.rutebanken.sobek.diff.generic;


import com.google.common.collect.Sets;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.Vehicle;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericObjectDifferTest {

    private static final GenericObjectDiffer genericObjectDiffer = new GenericObjectDiffer();

    @Test
    public void diffChangeName() throws IllegalAccessException {
        Vehicle oldVehicle = new Vehicle();
        oldVehicle.setName(new EmbeddableMultilingualString("old name"));
        Vehicle newVehicle = new Vehicle();
        newVehicle.setName(new EmbeddableMultilingualString("new name"));

        String diffString = compareObjectsAndPrint(oldVehicle, newVehicle);
        assertThat(diffString)
                .contains(newVehicle.getName().getValue())
                .contains(oldVehicle.getName().getValue());
    }

    @Test
    public void maxDepth() throws IllegalAccessException {

        RecursiveObject recursiveObject1 = new RecursiveObject();
        RecursiveObject recursiveObject2 = new RecursiveObject();

        addRecursively(recursiveObject1, 0, 20);
        addRecursively(recursiveObject2, 20, 40);


        List<Difference> differences = genericObjectDiffer.compareObjects(recursiveObject1, recursiveObject2, commonDiffConfig);
        System.out.println(genericObjectDiffer.diffListToString(differences));

        assertThat(differences).hasSize(10);

    }

    private static GenericDiffConfig commonDiffConfig = GenericDiffConfig.builder()
                .onlyDoEqualsCheck(Sets.newHashSet(Geometry.class))
                .ignoreFields(Sets.newHashSet("id"))
                .identifiers(Sets.newHashSet("netexId", "ref"))
                .build();

    public void addRecursively(RecursiveObject recursiveObject, int depth, int maxDepth) {

        if(depth >= maxDepth) {
            return;
        }
        RecursiveObject newChild = new RecursiveObject();
        newChild.value = depth;
        recursiveObject.child = newChild;
        addRecursively(newChild, ++depth, maxDepth);
    }

    public String compareObjectsAndPrint(Object oldObject, Object newObject) throws IllegalAccessException {


        List<Difference> differences = genericObjectDiffer.compareObjects(oldObject, newObject, commonDiffConfig);
        String diff = genericObjectDiffer.diffListToString(differences);

        System.out.println("-----------");
        System.out.println(diff);
        System.out.println("-----------");
        return diff;
    }

    private static class RecursiveObject {

        private RecursiveObject child;

        public int value;

        public RecursiveObject() {
        }

        @Override
        public String toString() {
            return "depth: "+ value;
        }


    }
}
