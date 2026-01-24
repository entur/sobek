# Annotation Summary for Sobek src/main/java

*Generated: 2026-01-24*
*Excludes: Lombok annotations (@Getter, @Setter, @Data, @Builder, @Slf4j, etc.)*

## Spring Framework Annotations

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Component` | 123 | Marks class as Spring-managed bean, auto-detected via component scanning | **Startup** - bean created at context initialization |
| `@Autowired` | 121 | Injects dependencies automatically by type | **Startup** - wired during bean creation |
| `@Value` | 47 | Injects values from properties/environment | **Startup** - resolved at bean creation |
| `@Bean` | 28 | Defines a bean in `@Configuration` class | **Startup** - factory method called at context init |
| `@Service` | 25 | Specialization of `@Component` for service layer | **Startup** - bean created at context init |
| `@Transactional` | 21 | Wraps method/class in database transaction | **Runtime** - proxy intercepts each method call |
| `@Profile` | 15 | Conditionally loads bean based on active profile | **Startup** - evaluated during bean registration |
| `@Configuration` | 11 | Marks class as source of bean definitions | **Startup** - processed early in context init |
| `@Repository` | 6 | Specialization for data access layer, adds exception translation | **Startup** - bean created at context init |
| `@ConditionalOnProperty` | 6 | Loads bean only if property condition met | **Startup** - evaluated during auto-config |
| `@Qualifier` | 3 | Disambiguates which bean to inject when multiple candidates exist | **Startup** - resolved during wiring |
| `@ConditionalOnMissingBean` | 2 | Loads bean only if no other bean of same type exists | **Startup** - evaluated during auto-config |
| `@Lazy` | 1 | Delays bean initialization until first accessed | **Lazy** - deferred until first use |
| `@SpringBootApplication` | 1 | Combines `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan` | **Startup** - triggers entire boot process |
| `@EntityScan` | 1 | Specifies packages to scan for JPA entities | **Startup** - entity discovery |
| `@ComponentScan` | 1 | Defines packages to scan for components | **Startup** - bean discovery |
| `@EnableAutoConfiguration` | 1 | Enables Spring Boot auto-configuration | **Startup** - triggers auto-config |
| `@EnableTransactionManagement` | 1 | Enables `@Transactional` annotation processing | **Startup** - sets up TX infrastructure |
| `@EnableWebSecurity` | 1 | Enables Spring Security web configuration | **Startup** - security filter chain setup |
| `@EnableGlobalMethodSecurity` | 1 | Enables method-level security annotations | **Startup** - sets up security proxies |

---

## JPA/Hibernate Annotations (jakarta.persistence)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Column` | 74 | Maps field to database column with constraints | **Startup** - metadata parsed during EntityManager init |
| `@AttributeOverride` | 72 | Overrides embedded object's column mapping | **Startup** - metadata |
| `@Transient` | 61 | Excludes field from persistence | **Startup** - metadata |
| `@MappedSuperclass` | 44 | Marks class whose mapping info is inherited by entities | **Startup** - metadata |
| `@Entity` | 39 | Marks class as JPA entity mapped to database table | **Startup** - entity registration |
| `@Embedded` | 38 | Embeds another object's fields into this entity's table | **Startup** - metadata |
| `@AttributeOverrides` | 36 | Container for multiple `@AttributeOverride` | **Startup** - metadata |
| `@Enumerated` | 22 | Specifies how enum is persisted (STRING/ORDINAL) | **Startup** - metadata |
| `@OneToMany` | 15 | Defines one-to-many relationship | **Startup** - metadata, **Runtime** - lazy loading |
| `@PersistenceContext` | 8 | Injects EntityManager | **Startup** - injected at bean creation |
| `@Id` | 8 | Marks primary key field | **Startup** - metadata |
| `@Embeddable` | 7 | Marks class that can be embedded in entities | **Startup** - metadata |
| `@ManyToOne` | 6 | Defines many-to-one relationship | **Startup** - metadata, **Runtime** - lazy loading |
| `@GeneratedValue` | 6 | Specifies ID generation strategy | **Startup** - metadata |
| `@Inheritance` | 3 | Specifies inheritance strategy (SINGLE_TABLE, JOINED, etc.) | **Startup** - metadata |
| `@Converter` | 2 | Marks class as JPA attribute converter | **Startup** - converter registration |
| `@Convert` | 2 | Applies converter to a field | **Startup** - metadata |
| `@JoinColumn` | 2 | Specifies foreign key column for relationships | **Startup** - metadata |
| `@OneToOne` | 2 | Defines one-to-one relationship | **Startup** - metadata |
| `@SequenceGenerator` | 1 | Defines database sequence for ID generation | **Startup** - metadata |
| `@PrePersist` | 1 | Callback before entity is persisted | **Runtime** - called on each persist |
| `@NoRepositoryBean` | 1 | Excludes interface from repository auto-creation | **Startup** - repository scanning |
| `@Index` | 1 | Defines database index on entity | **Startup** - DDL metadata |
| `@IdClass` | 1 | Specifies composite primary key class | **Startup** - metadata |
| `@ElementCollection` | 1 | Maps collection of basic/embeddable types | **Startup** - metadata |
| `@JoinTable` | 1 | Specifies join table for many-to-many | **Startup** - metadata |
| `@ManyToMany` | 1 | Defines many-to-many relationship | **Startup** - metadata |
| `@CollectionTable` | 1 | Specifies table for `@ElementCollection` | **Startup** - metadata |
| `@EntityListeners` | 1 | Registers lifecycle callback listeners for entity | **Startup** - metadata, **Runtime** - callbacks |

---

## Hibernate-Specific Annotations (org.hibernate)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Cascade` | 1 | Hibernate-specific cascade operations | **Startup** - metadata |
| `@GenericGenerator` | 1 | Defines custom ID generator | **Startup** - generator registration |

---

## JAX-RS/Jersey Annotations (jakarta.ws.rs)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Produces` | 16 | Declares MIME types endpoint can produce | **Startup** - REST metadata |
| `@Path` | 15 | Maps URL path to resource class/method | **Startup** - route registration |
| `@GET` | 10 | Marks method as HTTP GET handler | **Startup** - route registration |
| `@QueryParam` | 7 | Binds query parameter to method argument | **Runtime** - extracted per request |
| `@BeanParam` | 5 | Aggregates multiple params into bean | **Runtime** - populated per request |
| `@POST` | 4 | Marks method as HTTP POST handler | **Startup** - route registration |
| `@Consumes` | 4 | Declares MIME types endpoint can consume | **Startup** - REST metadata |
| `@Provider` | 3 | Registers JAX-RS extension (converter, filter, etc.) | **Startup** - provider registration |
| `@PathParam` | 2 | Binds path segment to method argument | **Runtime** - extracted per request |

---

## OpenAPI/Swagger Annotations (io.swagger.v3)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Schema` | 10 | Documents model properties in OpenAPI spec | **Startup** - API doc generation |
| `@Tag` | 8 | Groups API operations by tag | **Startup** - API doc generation |
| `@Parameter` | 8 | Documents API parameter | **Startup** - API doc generation |
| `@Operation` | 3 | Documents API operation | **Startup** - API doc generation |
| `@ApiResponse` | 3 | Documents possible API response | **Startup** - API doc generation |

---

## Jackson Annotations (com.fasterxml.jackson)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@JsonIgnore` | 1 | Excludes field from JSON serialization | **Runtime** - checked during serialize/deserialize |
| `@JsonInclude` | 1 | Controls inclusion of null/empty values in JSON | **Runtime** - checked during serialization |

---

## JAXB Annotations (jakarta.xml.bind)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@XmlRootElement` | 2 | Marks class as XML root element | **Runtime** - XML marshalling |

---

## Jakarta Common Annotations (jakarta.annotation)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@PostConstruct` | 2 | Callback after bean initialization | **Startup** - called after dependency injection |
| `@PreDestroy` | 1 | Callback before bean destruction | **Shutdown** - called during context close |

---

## CDI/Inject (jakarta.inject)

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Inject` | 1 | Standard JSR-330 dependency injection | **Startup** - wired during bean creation |

---

## Java Standard Annotations

| Annotation | Count | Use Case | Startup/Runtime |
|------------|-------|----------|-----------------|
| `@Override` | 234 | Compiler hint - method overrides parent | **Compile-time** - no runtime effect |
| `@SuppressWarnings` | 12 | Suppresses compiler warnings | **Compile-time** - no runtime effect |
| `@Deprecated` | 1 | Marks element as deprecated | **Compile-time** - warning only |
| `@Nullable` | 1 | Indicates parameter/return can be null | **Compile-time** - static analysis |

---

## Summary by Category

| Category | Total Count | Startup Impact |
|----------|-------------|----------------|
| **JPA/Hibernate** | ~340 | High - all metadata parsed at EntityManagerFactory init |
| **Spring Core DI** | ~250 | High - beans created at context startup |
| **JAX-RS/Jersey** | ~66 | Medium - routes registered at startup |
| **Spring Boot Conditional** | ~25 | Medium - evaluated during auto-config |
| **OpenAPI/Swagger** | ~32 | Low - documentation generation |
| **Jackson/XML** | ~4 | None at startup - runtime only |
| **Java Standard** | ~248 | None - compile-time only |

**Total unique annotations (excluding Lombok/Javadoc):** ~55

## Key Findings

- Most annotations are **startup-time**: JPA metadata, Spring bean registration, route mapping
- Only `@Transactional`, Jackson, JAXB, and JPA lifecycle callbacks (`@PrePersist`) are **runtime**
- `@Lazy` (1 usage) is the only annotation that explicitly defers initialization
- Heavy use of `@AttributeOverride` (72) indicates complex embedded object mappings
