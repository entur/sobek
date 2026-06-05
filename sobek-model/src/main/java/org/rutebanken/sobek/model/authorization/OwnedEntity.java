package org.rutebanken.sobek.model.authorization;


public interface OwnedEntity {
    String getDataOwnerRef();
    void setDataOwnerRef(String dataOwnerRef);
}
