package org.rutebanken.sobek.rest.validationdata;

import lombok.extern.slf4j.Slf4j;
import org.rutebanken.sobek.netex.ValidNeTExIdProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Component
@RestController
@RequestMapping("/services/validation/netexids")
@Slf4j
public class NeTExIdController {

    private final ValidNeTExIdProvider validNeTExIdProvider;

    public NeTExIdController(ValidNeTExIdProvider validNeTExIdProvider) {
        this.validNeTExIdProvider = validNeTExIdProvider;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public ResponseEntity<List<String>> getAllValidNeTExIds() {
        log.info("Providing a list of all currently valid NeTEx Ids");

        List<String> validNeTExIds = validNeTExIdProvider.getValidNeTExIds();

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8"))
                .body(validNeTExIds);
    }
}