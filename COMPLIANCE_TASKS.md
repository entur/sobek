# Sobek Compliance Tasks

Gaps identified against Entur API guidelines and development best practices. Check items as completed.

## High Priority - API Structure

- [ ] **Create OpenAPI spec file** - Currently Swagger generates dynamically; need static spec
  - Add `x-entur-metadata.id` (SHOULD per guidelines)
  - File: New `openapi.yaml` or configure Swagger output

- [ ] **Add API versioning** - REST paths missing `/v1/`
  - Current: `/services/vehicles/netex`
  - Required: `/services/vehicles/v1/netex`
  - Files: `config/JerseyConfig.java`, all `@Path` annotations in resource classes

- [ ] **Implement RFC 9457 error responses** - Current format non-compliant
  - Must use `application/problem+json` content type
  - Must include `title` and `status` fields (required by RFC 9457)
  - Should include `type`, `detail`, and `instance` fields
  - Current: Returns `ErrorResponseEntity` with `errors[].message` structure
  - Files: `rest/exception/GeneralExceptionMapper.java`, `rest/exception/ErrorResponseEntity.java`

## Medium Priority - API Documentation

- [ ] **Document JWT security scheme in OpenAPI**
  - Auth implemented but not documented in spec
  - Add `securitySchemes` with `jwt` bearer format
  - Files: OpenAPI configuration classes

- [ ] **Add `x-entur-permissions` extension** - Required for partner endpoints
  - Document required permissions per endpoint
  - Files: REST resource classes need `@Operation` annotations

- [ ] **Add `@Operation` annotations** - Only health/prometheus currently annotated
  - `rest/netex/publicationdelivery/VehicleImportResource.java` (line 78: POST method missing @Operation)
  - `rest/netex/publicationdelivery/VehicleExportResource.java`
  - `rest/netex/publicationdelivery/AsyncExportResource.java`
  - `rest/netex/publicationdelivery/AutosysAPIResource.java`

- [ ] **Expand JavaDoc coverage** - Currently limited coverage
  - Focus on public service interfaces
  - Priority: `AuthorizationService`, `VehicleVersionedSaverService`

- [ ] **Review GraphQL backward compatibility**
  - Check schema changes don't break existing clients
  - Document deprecation patterns

## Low Priority - Code Quality

- [ ] **Consider records for DTOs** - Currently using Lombok @Data
  - Evaluate which DTOs can be immutable records
  - JPA entities must remain mutable

- [ ] **Expand unit test coverage**
  - Integration tests good; unit tests could increase
  - Target: business logic in service layer

- [ ] **Standardize on constructor injection**
  - Currently uses mix of `@Autowired` field injections
  - Prefer constructor injection for testability

---

## Reference Files

| File | Location | Compliance Area |
|------|----------|-----------------|
| JerseyConfig.java | `src/main/java/org/rutebanken/sobek/config/` | API paths, versioning |
| GeneralExceptionMapper.java | `src/main/java/org/rutebanken/sobek/rest/exception/` | Error handling format |
| ErrorResponseEntity.java | `src/main/java/org/rutebanken/sobek/rest/exception/` | Error response structure |
| VehicleImportResource.java | `src/main/java/org/rutebanken/sobek/rest/netex/publicationdelivery/` | OpenAPI annotations |
| VehicleExportResource.java | `src/main/java/org/rutebanken/sobek/rest/netex/publicationdelivery/` | OpenAPI annotations |
| AsyncExportResource.java | `src/main/java/org/rutebanken/sobek/rest/netex/publicationdelivery/` | OpenAPI annotations |
| AutosysAPIResource.java | `src/main/java/org/rutebanken/sobek/rest/netex/publicationdelivery/` | OpenAPI annotations |
| RegisterGraphQLSchema.java | `src/main/java/org/rutebanken/sobek/rest/graphql/` | GraphQL patterns |

## Source Guidelines

- `ENTUR_API_guidelines.md` - REST API standards (MUST/SHOULD/MAY requirements)
- `ENTUR_dev_decision_records.md` - Development best practices
