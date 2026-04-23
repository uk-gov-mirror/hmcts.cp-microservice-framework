# JNDI Configuration Reference — cp-microservice-framework

JNDI values are injected at application startup via CDI producers:

- `@GlobalValue` — resolved from `java:global/<key>`. Shared across all deployed applications in the WildFly instance.
- `@Value` — resolved first from `java:app/<service-context-name>/<key>`, falling back to `java:global/<key>`. Intended for per-service overrides.

If a key is not present in JNDI and no `defaultValue` is set, the application will fail to start with a `MissingPropertyException`.

---

## Feature Control

All values are **global** (`@GlobalValue`). Configured in `FeatureControlConfiguration`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `feature-control.enabled` | Global | `false` | Master switch to enable remote feature-flag control across all services. |
| `feature-control-cache.enabled` | Global | `false` | Enable local in-memory caching of feature-control flag values. |
| `feature-control-cache-refresh-rate.timer.interval.milliseconds` | Global | `600000` | How often (ms) the feature-control cache is refreshed from the remote source. |
| `feature-control-cache-refresh-rate.timer.start.wait.milliseconds` | Global | `0` | Initial delay (ms) before the first feature-control cache refresh fires after startup. |

---

## REST Dispatcher

**Global** (`@GlobalValue`). Configured in `DispatcherConfiguration`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `rest.dispatcher.response.json.validation.enabled` | Global | `false` | When `true`, the REST dispatcher validates outbound response payloads against their JSON schema. |

---

## Envelope Validation

**Global** (`@GlobalValue`). Configured in `EnvelopeValidationExceptionHandlerProducer`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `envelope.validation.exception.handler` | Global | `uk.gov.justice.services.core.envelope.EmptyValidationExceptionHandler` | Fully-qualified class name of the handler invoked when envelope schema validation fails. Override to provide custom error handling (e.g. logging, dead-letter routing). |

---

## JMS / Messaging

**Global** (`@GlobalValue`). Configured in `JmsConnectionConfig`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `jms.connection.audit.message.broker.enabled` | Global | `false` | When `true`, audit messages are sent to a separate dedicated Artemis broker rather than the default broker. |

**App-specific** (`@Value`). Configured in `JmsMessagingConfiguration`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `messaging.jms.oversize.message.threshold.bytes` | App | `262144` | JMS message size in bytes above which a warning is raised for oversize envelopes (default 256 KB). |

---

## Event Processing

**App-specific** (`@Value`). Configured in `EventPullConfiguration` and `DefaultEventErrorHandlingConfiguration`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `event.processing.by.pull.mechanism.enabled` | App | `false` | Switch event consumption from JMS push delivery to REST-based pull mechanism. |
| `event.stream.self.healing.enabled` | App | `false` | Enable automatic retry and self-healing for event streams that have entered an error state. |

---

## Metrics (Micrometer)

**App-specific** (`@Value`). Configured in `MetricsConfiguration`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `micrometer.metrics.enabled` | App | `false` | Enable Micrometer metrics collection. |
| `azure.metrics.monitor.connection.string` | App | `azure-metrics-connection-string-not-set` | Azure Monitor connection string used to publish metrics. Must be set in environments where metrics are collected. |
| `micrometer.metrics.statistic.timer.interval.milliseconds` | App | `60000` | How often (ms) the metrics statistics timer fires to flush collected metrics. |
| `micrometer.metrics.statistic.timer.delay.milliseconds` | App | `10000` | Initial delay (ms) before the metrics statistics timer starts. |
| `micrometer.metrics.env` | App | `local` | Environment label attached to all published metrics (e.g. `prod`, `staging`, `local`). |

---

## Auditing

**App-specific** (`@Value`). Configured in `DefaultAuditService`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `audit.blacklist` | App | *(empty)* | Regular expression pattern matched against action names. Matching actions are excluded from audit recording. |

---

## Schema Validation

**App-specific** (`@Value`). Configured in `BackwardsCompatibleJsonSchemaValidator`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `schema.validation.action.whitelist` | App | `audit.events.audit-recorded` | Comma-separated action names that bypass schema-catalog validation and fall back to file-based schema lookup. |

---

## CORS

**App-specific** (`@Value`). Configured in `CorsFeature`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `corsAllowedMethods` | App | `GET, POST, DELETE, PUT, OPTIONS` | HTTP methods permitted by the CORS filter. |
| `corsAllowedOrigin` | App | `*` | Origins permitted by the CORS filter. Set to a specific domain in production. |
| `corsAllowedHeaders` | App | `Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, CJSCPPUID, sessionId, cppClientCorrelationId` | HTTP request headers permitted by the CORS filter. |

---

## JDBC

**App-specific** (`@Value`). Configured in `PreparedStatementWrapper`.

| JNDI Key | Scope | Default | Description |
|---|---|---|---|
| `jdbc.statement.fetchSize` | App | `200` | JDBC fetch size (rows per network round-trip) applied to prepared statement result sets. Increase for large result set queries. |
