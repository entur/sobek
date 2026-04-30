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

/* Copied from UTTU and refined for Sobek needs */

package org.rutebanken.sobek.error;

import java.util.HashMap;
import java.util.Map;

public class CodedError {

    private final ErrorCode errorCode;
    private final SubCode subCode;
    private final Map<String, Object> metadata;

    public static CodedError fromErrorCode(ErrorCode errorCode) {
        return new CodedError(errorCode, null, null);
    }

    public static CodedError fromErrorCode(ErrorCode errorCode, SubCode subCode) {
        return new CodedError(errorCode, subCode, null);
    }

    public static CodedError fromErrorCode(
            ErrorCode errorCode,
            SubCode subCode,
            Map<String, Object> metadata
    ) {
        return new CodedError(errorCode, subCode, metadata);
    }

    CodedError(ErrorCode errorCode, SubCode subCode, Map<String, Object> metadata) {
        this.errorCode = errorCode;
        this.subCode = subCode;
        this.metadata = metadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public SubCode getSubCode() {
        return subCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", errorCode);
        map.put("subCode", subCode);
        map.put("metadata", metadata);
        return map;
    }
}
