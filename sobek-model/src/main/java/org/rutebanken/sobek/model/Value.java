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

package org.rutebanken.sobek.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@ToString
public class Value implements Serializable {

    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    private long id;

    @ElementCollection
    @CollectionTable(
            name = "value_items",
            indexes = @Index(name = "items_index", columnList = "items")
    )
    private Set<String> items = new HashSet<>();

    public Value() {
    }

    public Value(String... items) {
        Collections.addAll(this.items, items);
    }

    public Value(List<String> items) {
        this.items.addAll(items);
    }

    public Set<String> getItems() {
        return items;
    }

    public void setItems(Set<String> items) {
        this.items = items;
    }

}
