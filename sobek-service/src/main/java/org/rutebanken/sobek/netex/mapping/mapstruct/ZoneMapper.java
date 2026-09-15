package org.rutebanken.sobek.netex.mapping.mapstruct;

import com.google.common.primitives.Longs;
import net.opengis.gml._3.PolygonType;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.*;
import org.rutebanken.netex.model.Zone_VersionStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

/**
 * MapStruct mapper for Zone_VersionStructure.
 * Handles mapping between NeTEx Zone_VersionStructure and Sobek Zone_VersionStructure entity.
 */
@Mapper(
  config = SobekMapperConfig.class,
  uses = {
    DataManagedObjectStructureMapper.class,
    PointRefStructureMapper.class,
    PolygonMapper.class,
    EntityInVersionMapper.class
  }
)
public interface ZoneMapper {
  /**
   * Maps from NeTEx Zone_VersionStructure to Sobek entity.
   */
  @ToSobekMappings
  org.rutebanken.sobek.model.Zone_VersionStructure mapToSobek(
    Zone_VersionStructure source,
    @Context MappingContext context
  );

  /**
   * Maps from Sobek entity back to NeTEx Zone_VersionStructure.
   */
  @ToNetexMappings
  Zone_VersionStructure mapToNetex(
    org.rutebanken.sobek.model.Zone_VersionStructure source,
    @Context MappingContext context
  );

  /**
   * Updates an existing Sobek entity from NeTEx structure.
   */
  @ToSobekMappings
  void updateSobekFromNetex(
    Zone_VersionStructure source,
    @MappingTarget org.rutebanken.sobek.model.Zone_VersionStructure target,
    @Context MappingContext context
  );

  @AfterMapping
  default void afterMapToSobek(
    Zone_VersionStructure source,
    @MappingTarget org.rutebanken.sobek.model.Zone_VersionStructure target,
    @Context MappingContext context
  ) {
    if (target != null) {
      context
        .getDataManagedObjectStructureMapper()
        .afterMappingToSobek(source, target, context);
      if (source.getCentroid() != null) {
        Point point = context
          .getSimplePointMapper()
          .simplePointToPoint(source.getCentroid(), context);
        target.setCentroid(point);
      }
    }
  }

  @AfterMapping
  default void afterMapToNetex(
    org.rutebanken.sobek.model.Zone_VersionStructure source,
    @MappingTarget Zone_VersionStructure target,
    @Context MappingContext context
  ) {
    if (target != null) {
      context
        .getDataManagedObjectStructureMapper()
        .afterMappingToNetex(source, target, context);
      target.setCentroid(
        context
          .getSimplePointMapper()
          .pointToSimplePoint(source.getCentroid(), context)
      );
    }
  }

  @Mapping(target = "id", ignore = true) // Handle in AfterMapping
  @Mapping(target = "netexId", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "version",
    source = "version",
    qualifiedByName = "versionToSobek"
  )
  @Mapping(target = "keyValues", ignore = true) // Handle in AfterMapping
  @Mapping(target = "centroid", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "polygon",
    source = "polygon",
    qualifiedByName = "polygonTypeToPolygon"
  )
  @interface ToSobekMappings {
  }

  @Mapping(target = "id", source = "netexId")
  @Mapping(target = "keyList", ignore = true) // Handle in AfterMapping
  @Mapping(target = "centroid", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "polygon",
    source = "polygon",
    qualifiedByName = "polygonToPolygonType"
  )
  @interface ToNetexMappings {
  }
}
