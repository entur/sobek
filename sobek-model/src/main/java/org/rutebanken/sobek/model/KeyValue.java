package org.rutebanken.sobek.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class KeyValue {

    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    private Long id;

    private String key;
    private String value;

    public KeyValue() {
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
