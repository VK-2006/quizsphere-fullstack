# Lombok compilation fix

The original build did not explicitly register Lombok as an annotation processor. On JDK 23 and newer this causes generated builders, constructors, getters, and setters to be missing during compilation.

The corrected `backend/pom.xml` now:

- pins Lombok to `1.18.46`;
- uses `provided` scope;
- configures `maven-compiler-plugin` 3.14.1;
- sets annotation processing to `full`;
- explicitly adds Lombok under `annotationProcessorPaths`.

Run from `backend`:

```bat
mvn clean
mvn -U spring-boot:run
```
