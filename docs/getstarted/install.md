# Install

Ragas Java is currently distributed as a source build. Use Maven to build and run locally.

## Prerequisites

- Java 21
- Maven 3.9+

## Build from source

```bash
mvn clean verify
```

## Run the API

```bash
mvn -pl ragas-api -am spring-boot:run
```

## Run tests

```bash
mvn verify
```

## Security scan

```bash
mvn -Psecurity-scan verify
```
