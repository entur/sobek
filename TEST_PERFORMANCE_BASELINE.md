# Test performance baseline

- Date: 2026-11-10
- Branch: perf/test-speed (from main)
- Command: `mvn test` (full reactor, sequential, Docker/Testcontainers, Windows 11)
- Result: BUILD SUCCESS, 448 tests, 0 failures/errors/skipped (32 + 310 + 106 per module summary)
- Wall clock: 268 s (Maven reports 04:16 min)

## Time per module

| Module | Time |
|---|---|
| Sobek (parent) | 1.0 s |
| Sobek Model | 1.5 s |
| Sobek Common | 37.5 s |
| Sobek Repository | 4.0 s |
| Sobek Service | 80 s |
| Sobek API (sobek-app) | 128 s |

## Slowest test classes (surefire "Time elapsed")

| Class | Time |
|---|---|
| repository.ReferenceResolverTest | 50.7 s |
| model.VersionOfObjectRefStructureTest | 35.8 s |
| rest.graphql.GraphQLMutationsTest | 16.6 s |
| exporter.StreamingPublicationDeliveryTest | 16.4 s |
| diff.generic.GenericObjectDifferTest | 15.3 s |
| rest.graphql.GraphQLPagedQueriesTest | 3.6 s |
| rest.graphql.TrainGraphQLTest | 3.3 s |
| auth.DefaultAuthorizationServiceTest | 2.5 s |

Everything else is under 2 s per class. Per-class times include Spring context / container start-up for the first class that uses it, so they are not pure test time.
