package org.rutebanken.sobek.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CodeValue {

    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    private Long id;

    private String valueType;
    private String label;
    private String value;

    public CodeValue() {
    }
}
