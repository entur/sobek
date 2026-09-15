package org.rutebanken.sobek.netex.mapping.mapstruct;

import com.google.common.primitives.Longs;
import net.opengis.gml._3.PolygonType;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.*;
import org.rutebanken.netex.model.Zone_VersionStructure;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
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

  @Named("versionToSobekZ")
  default Long versionToSobek(String version) {
    if (version != null) {
      if (version.equals("any")) {
        return -1L; // Need to handle this value in import.
      } else {
        Long longVersion = Longs.tryParse(version);
        if (longVersion != null) {
          return longVersion;
        } else {
          throw new NetexMappingException(
            "Received version in netex format. " +
            "But cannot parse version. Expecting a long value or the String 'any'. " +
            "Value is: " +
            version
          );
        }
      }
    } else {
      return null;
    }
  }

  @Mapping(target = "id", ignore = true) // Handle in AfterMapping
  @Mapping(target = "netexId", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "version",
    source = "version",
    qualifiedByName = "versionToSobekZ"
  )
  @Mapping(target = "keyValues", ignore = true) // Handle in AfterMapping
  @Mapping(target = "centroid", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "polygon",
    source = "polygon",
    qualifiedByName = "polygonTypeToPolygonZ"
  )
  @interface ToSobekMappings {
  }

  @Mapping(target = "id", source = "netexId")
  @Mapping(target = "keyList", ignore = true) // Handle in AfterMapping
  @Mapping(target = "centroid", ignore = true) // Handle in AfterMapping
  @Mapping(
    target = "polygon",
    source = "polygon",
    qualifiedByName = "polygonToPolygonTypeZ"
  )
  @interface ToNetexMappings {
  }

  @Named("polygonTypeToPolygonZ")
  default Polygon polygonTypeToPolygonZ(
    PolygonType polygonType,
    @Context MappingContext context
  ) {
    return context.getPolygonMapper().polygonTypeToPolygon(polygonType);
  }

  @Named("polygonToPolygonTypeZ")
  default PolygonType polygonToPolygonTypeZ(
    Polygon polygon,
    @Context MappingContext context
  ) {
    return context.getPolygonMapper().polygonToPolygonType(polygon);
  }
}
