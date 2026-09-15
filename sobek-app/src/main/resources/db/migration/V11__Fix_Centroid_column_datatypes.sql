ALTER TABLE deck_space
    ALTER COLUMN centroid TYPE public.GEOMETRY USING ST_GeomFromWKB(centroid);

ALTER TABLE locatable_spot
    ALTER COLUMN centroid TYPE public.GEOMETRY USING ST_GeomFromWKB(centroid);
