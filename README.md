# cp-microservice-framework

`uk.gov.justice.services:microservice-framework`

The core Java framework for building CQRS and Event Sourcing microservices on WildFly. All `cpp-context-*` bounded-context services are built on top of this framework.

## Position in the hierarchy

```
maven-framework-parent-pom
└── cp-microservice-framework  ← this project
    └── framework-bom  (imported by cp-event-store, cpp-platform-libraries, cpp-context-*)
```

This project imports `framework-libraries-bom` from `cp-framework-libraries` and publishes `framework-bom` for downstream consumers.

## Modules

| Module | Artifact | Description |
|---|---|---|
| `core` | `core` | Central dispatch engine: `DefaultDispatcher`, interceptor chain, handler registry, `@Handles` resolver |
| `components` | `components` | `@ServiceComponent` CDI qualifier definitions and component scanning |
| `common` | `common` | Shared utilities — UUID helpers, stream utilities, annotation scanning |
| `common-rest` | `common-rest` | REST adapter base classes, CORS filter (`CorsFeature`), JAX-RS application config |
| `event-subscription` | `event-subscription` | Subscription manager — stream processing workers, circuit breaker, retry/back-off |
| `event-subscription-test-utils` | `event-subscription-test-utils` | Test utilities for subscription/stream processing scenarios |
| `framework-generators` | `*-generator-plugin`, `*-adapter-generator` | RAML-to-Java code generators: REST adapter, JMS listener, messaging adapter, REST client, direct client, unified-search client |
| `framework-jmx-command-client` | `framework-jmx-command-client` | JMX client for triggering administrative commands against a running service |
| `framework-system` | `framework-system` | System-level CDI beans: JNDI producers (`@Value`, `@GlobalValue`), startup/shutdown hooks |
| `framework-utilities` | `framework-utilities` | Framework-internal utilities: envelope builders, ID generators, format helpers |
| `jmx` | `jmx` | JMX MBean definitions and WildFly JMX integration |
| `messaging` | `messaging-jms`, `messaging-core` | JMS messaging: Artemis connection factory, message consumer/producer, oversize-message detection |
| `metrics` | `metrics-micrometer` | Micrometer timer/counter wrappers for framework dispatch metrics |
| `persistence` | `persistence-datasource`, `persistence-jdbc` | JPA datasource producers, prepared statement wrapper (configurable fetch size) |
| `raml-lint-check` | `raml-lint-check` | Build-time RAML schema validation — enforces that every schema has a matching example file |
| `test-utils` | `test-utils-core`, `test-utils-common`, `test-utils-persistence`, `test-utils-messaging` | Test utilities: envelope builders, in-memory handler registry, embedded datasource, JMS test helpers |
| `framework-bom` | `framework-bom` | BOM that imports all `microservice-framework` artifacts at a consistent version |

## Key patterns

### Command flow

```
REST POST → @Handles("context.commands.do-something") → Aggregate → raise event → Event Store
```

A command handler is a CDI bean annotated with `@ServiceComponent(COMMAND_HANDLER)`. Each handler method carries `@Handles("action.name")` and receives a `JsonEnvelope`:

```java
@ServiceComponent(COMMAND_HANDLER)
public class DoSomethingCommandHandler {

    @Inject
    private EventStream eventStream;

    @Handles("my-context.commands.do-something")
    public void handle(final JsonEnvelope command) {
        final MyAggregate aggregate = /* ... rebuild from stream ... */;
        aggregate.doSomething(command.payloadAsJsonObject());
        eventStream.append(aggregate.streamId(), aggregate.getRaisedEvents());
    }
}
```

### Event listener (private — viewstore update)

```java
@ServiceComponent(EVENT_LISTENER)
public class SomethingHappenedEventListener {

    @Handles("my-context.events.something-happened")
    public void handle(final JsonEnvelope event) {
        // update viewstore
    }
}
```

### Query handler

```java
@ServiceComponent(QUERY_VIEW)
public class MyQueryView {

    @Handles("my-context.queries.get-something")
    public JsonEnvelope handle(final JsonEnvelope query) {
        return envelopeFrom(metadataOf(query), someJsonObject);
    }
}
```

### JNDI configuration

Framework system properties are injected at startup via CDI producers:

- `@GlobalValue("key")` — resolves from `java:global/<key>` (WildFly-wide)
- `@Value("key")` — resolves from `java:app/<service-context>/<key>` with fallback to `java:global/<key>`

See [jndi-configuration.md](./jndi-configuration.md) for all configurable keys.

## Build

```bash
# Build and install
mvn clean install

# Skip integration tests (requires PostgreSQL)
mvn clean install -Drun.it=false

# Run integration tests (requires Docker infrastructure running)
mvn clean install -Pintegration-test
```

Integration tests require PostgreSQL with a `framework` user and databases `frameworkeventstore`, `frameworkviewstore`, and `frameworkfilestore`. Use `cpp-developers-docker` to spin up the full local infrastructure.
