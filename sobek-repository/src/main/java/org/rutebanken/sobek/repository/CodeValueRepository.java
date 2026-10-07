package org.rutebanken.sobek.repository;

import org.rutebanken.sobek.model.CodeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeValueRepository extends JpaRepository<CodeValue, Long> {
    Page<CodeValue> findAllByValueType(String valueType, Pageable pageable);
}

