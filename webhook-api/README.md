# webhook-api

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only
> at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the
`target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container
using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/webhook-api-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please
consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time
  processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or
  any of the extensions that depend on it.
- MongoDB with Panache ([guide](https://quarkus.io/guides/mongodb-panache)): Simplify your
  persistence code for MongoDB via the active record or the repository pattern
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your
  Quarkus application
- SmallRye JWT ([guide](https://quarkus.io/guides/security-jwt)): Secure your applications with JSON
  Web Token
- SmallRye Metrics ([guide](https://quarkus.io/guides/smallrye-metrics)): Expose metrics for your
  services
- MongoDB client ([guide](https://quarkus.io/guides/mongodb)): Connect to MongoDB in either
  imperative or reactive style
- Micrometer Registry Prometheus ([guide](https://quarkus.io/guides/micrometer)): Enable Prometheus
  support for Micrometer
- WebSockets Client ([guide](https://quarkus.io/guides/websockets)): Client for WebSocket
  communication channel
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (
  field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs
  with OpenAPI - comes with Swagger UI
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization
  support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or
  any of the extensions that depend on it
- WebSockets ([guide](https://quarkus.io/guides/websockets)): WebSocket communication channel
  support
- Mailer ([guide](https://quarkus.io/guides/mailer)): Send emails
- SmallRye JWT Build ([guide](https://quarkus.io/guides/security-jwt-build)): Create JSON Web Token
  with SmallRye JWT Build API
- Reactive Routes ([guide](https://quarkus.io/guides/reactive-routes)): REST framework offering the
  route model to define non blocking endpoints

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### WebSockets

WebSocket communication channel starter code

[Related guide section...](https://quarkus.io/guides/websockets)
