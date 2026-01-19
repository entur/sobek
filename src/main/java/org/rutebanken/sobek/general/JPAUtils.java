package org.rutebanken.sobek.general;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;

public class JPAUtils {
    public static <T> String getTableName(EntityManager em, Class<T> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            String name = entityClass.getAnnotation(Table.class).name();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        MetamodelImplementor metamodel = (MetamodelImplementor) sessionFactory.getMetamodel();
        EntityPersister persister = metamodel.entityPersister(entityClass);

        if (persister instanceof AbstractEntityPersister abstractPersister) {
            return abstractPersister.getRootTableName();
        }
        // Fallback to JPA Metamodel name if Hibernate persister is not an AbstractEntityPersister
        EntityType<T> entityType = em.getMetamodel().entity(entityClass);
        return entityType.getName();
    }
}



