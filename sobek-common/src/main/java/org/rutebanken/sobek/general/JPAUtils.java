package org.rutebanken.sobek.general;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.model.domain.spi.JpaMetamodelImplementor;

public class JPAUtils {
    public static <T> String getTableName(EntityManager em, Class<T> entityClass) {
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);

        // Access the underlying Hibernate MappingMetamodel from the JPA Metamodel
        JpaMetamodelImplementor jpaMetamodel = (JpaMetamodelImplementor)em.getMetamodel();
        MappingMetamodel mappingMetamodel = jpaMetamodel.getMappingMetamodel();

        // Get the entity persister which contains the actual table name
        String entityName = entityClass.getName();
        var entityPersister = mappingMetamodel
                .getEntityDescriptor(entityName);

        // This returns the actual physical table name
        return entityPersister.getTableName();
    }
}



