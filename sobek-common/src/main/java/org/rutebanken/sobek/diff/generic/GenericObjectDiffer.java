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
import javassist.util.proxy.MethodHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GenericObjectDiffer {

    private static final int MAX_DEPTH = 10;

    private static final Logger logger = LoggerFactory.getLogger(GenericObjectDiffer.class);

    public List<Difference> compareObjects(Object oldObject, Object newObject, GenericDiffConfig genericDiffConfig) throws IllegalAccessException {
        return compareObjects(null, oldObject, newObject, genericDiffConfig, 0);
    }

    public String diffListToString(List<Difference> differences) {
        return differences.stream().map(difference -> difference.toString()).collect(Collectors.joining("\n", "\n", "\n"));
    }

    private List<Difference> compareObjects(String property, Object oldObject, Object newObject, GenericDiffConfig genericDiffConfig, int depth) throws IllegalAccessException {

        List<Difference> differences = new ArrayList<>();

        // The depth is how deeply this object is nested, so it is passed down and not kept in the (shared) config:
        // the objects next to each other in a list or a map are not nested in one another.
        if (depth > MAX_DEPTH) {
            logger.debug("Reached max depth of {}", MAX_DEPTH);
            return differences;
        }

        Class clazz = oldObject.getClass();

        Field[] fields = getAllFields(clazz, genericDiffConfig.ignoreFields);
        Set<Field> newObjectFields = Sets.newHashSet(getAllFields(newObject.getClass(), genericDiffConfig.ignoreFields));
        if (property == null) {
            property = oldObject.getClass().getSimpleName();
        }

        for (Field field : fields) {

            try {
                if (!newObjectFields.contains(field)) {
                    logger.debug("Ignoring field {} as it not present in newObject of type {}", field, newObject.getClass());
                    continue;
                }
                if (field.getType().isAssignableFrom(MethodHandler.class)) {
                    logger.debug("Ignoring field {} as its assignable from {}", field, MethodHandler.class);
                    continue;
                }

                field.setAccessible(true);

                Object oldValue = field.get(oldObject);
                Object newValue = field.get(newObject);

                if (oldValue == null && newValue == null) {
                    continue;
                }

                if (oldValue == null && newValue != null || oldValue != null && newValue == null) {
                    differences.add(new Difference(property + '.' + field.getName(), oldValue, newValue));
                    continue;
                }

                if (Collection.class.isAssignableFrom(field.getType())) {
                    compareCollection(property + '.' + field.getName(), field.getName(), (Collection) oldValue, (Collection) newValue, differences, genericDiffConfig, depth + 1);
                    continue;

                } else if (Map.class.isAssignableFrom(field.getType())) {
                    String mapPropertyName = property + "." + field.getName();
                    compareMap((Map) oldValue, (Map) newValue, differences, false, mapPropertyName, genericDiffConfig, depth + 1);
                    continue;
                }

                if (oldValue == newValue) {
                    continue;
                }

                if (oldValue.equals(newValue)) {
                    continue;
                }

                String childProperty = property + '.' + field.getName();

                if (isPrimitive(oldValue)) {
                    differences.add(new Difference(childProperty, oldValue, newValue));
                } else if (Instant.class.isAssignableFrom(field.getType())) {
                    differences.add(new Difference(property + '.' + field.getName(), oldValue, newValue));
                } else if (genericDiffConfig.onlyDoEqualsCheck.stream().anyMatch(type -> type.isAssignableFrom(oldValue.getClass()))) {
                    if (!oldValue.equals(newValue)) {
                        differences.add(new Difference(childProperty, oldValue, newValue));
                    }
                } else {
                    differences.addAll(compareObjects(property + '.' + field.getName(), oldValue, newValue, genericDiffConfig, depth + 1));
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException("Could not compare property " + property
                                                   + ", field '" + field + ". ex:\""
                                                   + e.getMessage() + "\". old object "
                                                   + oldObject + " new object " + newObject, e);
            }
        }

        return differences;
    }

    private boolean isPrimitive(Object value) {
        return value instanceof Number || value instanceof String || value instanceof Boolean;
    }

    private void compareMap(Map<?, ?> map1, Map<?, ?> map2, List<Difference> differences, boolean reverse, String mapPropertyName, GenericDiffConfig genericDiffConfig, int depth) throws IllegalAccessException {

        Map<?, ?> leftMap;
        Map<?, ?> rightMap;

        if (reverse) {
            leftMap = map2;
            rightMap = map1;
        } else {
            leftMap = map1;
            rightMap = map2;
        }

        for (Object leftMapKey : leftMap.keySet()) {

            Object leftMapValue = leftMap.get(leftMapKey);

            if (!rightMap.containsKey(leftMapKey)) {
                logger.debug("right map does not contain key {}", leftMapKey);

                differences.add(new Difference(mapPropertyName + "{" + leftMapKey + "}", leftMapValue, null));

            } else if (rightMap.containsKey(leftMapKey)) {

                logger.debug("right map contain key {}", leftMapKey);

                String childProperty = mapPropertyName + "{" + leftMapKey + "}";
                differences.addAll(compareObjects(childProperty, leftMapValue, rightMap.get(leftMapKey), genericDiffConfig, depth));
            }
        }
    }

    private void compareCollection(final String propertyName, String fieldName, Collection oldCollection, Collection newCollection, List<Difference> differences, GenericDiffConfig genericDiffConfig, int depth) throws IllegalAccessException {

        if (oldCollection == null && newCollection == null) {
            return;
        }

        if (oldCollection == null && newCollection != null) {
            differences.add(new Difference(propertyName, null, newCollection.size()));
        } else if (oldCollection != null && newCollection == null) {
            differences.add(new Difference(propertyName, oldCollection.size(), null));
        } else if (oldCollection.isEmpty() && newCollection.isEmpty()) {
            return;
        } else if (genericDiffConfig.orderedCollections != null && genericDiffConfig.orderedCollections.contains(fieldName)) {
            compareOrderedCollection(propertyName, oldCollection, newCollection, differences, genericDiffConfig, depth);
        } else {
            Set<Object> ignoreIdentifiers = new HashSet<>();
            compareCollectionItems(propertyName, oldCollection, newCollection, differences, ignoreIdentifiers, false, genericDiffConfig, depth);
            compareCollectionItems(propertyName, newCollection, oldCollection, differences, ignoreIdentifiers, true, genericDiffConfig, depth);

        }
    }

    /**
     * For a collection where the position of an item is part of its meaning (e.g. the components of a train): the items
     * are compared by position, not by identifier. An item that moved is a change in both the positions it left and
     * took, and an item that has been added or removed is reported with its position.
     */
    private void compareOrderedCollection(String propertyName, Collection oldCollection, Collection newCollection, List<Difference> differences, GenericDiffConfig genericDiffConfig, int depth) throws IllegalAccessException {
        List<Object> oldItems = new ArrayList<>(oldCollection);
        List<Object> newItems = new ArrayList<>(newCollection);
        for (int position = 0; position < Math.max(oldItems.size(), newItems.size()); position++) {
            String itemProperty = propertyName + "[" + position + "]";
            if (position >= oldItems.size()) {
                differences.add(new Difference(DiffType.COLLECTION_ADD, itemProperty, null, newItems.get(position)));
            } else if (position >= newItems.size()) {
                differences.add(new Difference(DiffType.COLLECTION_REMOVE, itemProperty, oldItems.get(position), null));
            } else if (oldItems.get(position) != newItems.get(position)) {
                differences.addAll(compareObjects(itemProperty, oldItems.get(position), newItems.get(position), genericDiffConfig, depth));
            }
        }
    }

    private void compareCollectionItems(String propertyName, Collection collectionLeft, Collection collectionRight, List<Difference> differences, Set<Object> ignoreIdentifiers, boolean reverse, GenericDiffConfig genericDiffConfig, int depth) throws IllegalAccessException {

        for (Object itemLeft : collectionLeft) {

            Object itemLeftIdentifier;
            // Get identifierField for left item.
            Field identifierField = identifierField(genericDiffConfig.identifiers, getAllFields(itemLeft.getClass(), genericDiffConfig.ignoreFields));
            if (identifierField != null) {

                itemLeftIdentifier = identifierField.get(itemLeft);
                if (ignoreIdentifiers.contains(itemLeftIdentifier)) {
                    continue;
                }

            } else {
                itemLeftIdentifier = null;
            }

            boolean foundMatchOnId = false;
            for (Object itemRight : collectionRight) {
                if (identifierField != null && itemLeftIdentifier != null) {
                    Object itemRightIdentifier = identifierField.get(itemRight);
                    if (itemLeftIdentifier.equals(itemRightIdentifier)) {

                        String newProperty = propertyName + "[" + itemRightIdentifier + "]";
                        ignoreIdentifiers.add(itemLeftIdentifier);
                        differences.addAll(compareObjects(newProperty, itemLeft, itemRight, genericDiffConfig, depth));
                        foundMatchOnId = true;
                        break;
                    }
                }
            }

            if (!foundMatchOnId) {
                if (reverse && !collectionRight.contains(itemLeft)) {
                    differences.add(new Difference(DiffType.COLLECTION_ADD, propertyName + "[]", null, itemLeft));
                    break;

                } else if (!collectionRight.contains(itemLeft)) {
                    differences.add(new Difference(DiffType.COLLECTION_REMOVE, propertyName + "[]", itemLeft, null));
                }
            }
        }

    }

    private Field[] getAllFields(Class clazz, Set<String> ignoreFields) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(getAllFields(clazz.getSuperclass(), ignoreFields)));
        }
        return fields.stream()
                       .filter(field -> !ignoreFields.contains(field.getName()))
                       .collect(Collectors.toList()).toArray(new Field[]{});
    }

    private Field identifierField(Set<String> identifiers, Field[] fields) {
        return Stream.of(fields)
                       .filter(field -> identifiers.contains(field.getName()))
                       .peek(identifierField -> identifierField.setAccessible(true))
                       .findFirst()
                       .orElse(null);
    }

}
