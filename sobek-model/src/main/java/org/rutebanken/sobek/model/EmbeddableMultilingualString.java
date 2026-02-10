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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * MultilingualString is extended to make it possible to embed.
 */
@Embeddable
@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class EmbeddableMultilingualString implements Serializable {
    protected String value;

    @Column(length = 5)
    protected String lang;

    public EmbeddableMultilingualString() {
    }

    public EmbeddableMultilingualString(String value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    public EmbeddableMultilingualString(String value) {
        this.value = value;
    }

}
