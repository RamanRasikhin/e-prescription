# E-Prescription

A university team prototype for issuing prescriptions, viewing prescription history and dispensing medication.

I participated in the team project. Individual component ownership has not been reconstructed, so this repository does not attribute the entire implementation to me.

## Features

- JavaFX views for patients, doctors and pharmacists.
- Prescription creation, lookup and fulfillment.
- Medication inventory and database access objects.
- A separate NanoHTTPD server exposing selected workflows through HTTP.

## Stack

Java · JavaFX · SQLite/JDBC · Gson · NanoHTTPD

## Run

A Maven configuration has been added to replace machine-specific IDE library paths. Use JDK 17 and Maven:

```powershell
mvn compile
mvn javafx:run
```

The desktop entry point is `Main`. The HTTP entry point is `ServerAPI`; it can be started separately with:

```powershell
mvn compile exec:java -Dexec.mainClass=ServerAPI
```

The server listens on port 8080 and creates demo records on startup. Run it with a fresh local database when evaluating it; repeated startup is not a migration or production seeding mechanism. The GUI and server are separate entry points, not a confirmed client/server integration.

The database file is `apteka.db` in the working directory. Existing database files have been excluded from this copy. `OldMainTest` is a manual demonstration program, not an automated test suite. All Java sources compiled locally using JDK 17, cached JavaFX 17.0.6 and the archived dependency jars. The new Maven configuration uses JavaFX 17.0.12; its dependency resolution and GUI/API startup have not yet been validated.

## Design materials

`pliki niezwiazane z kodem/` contains use cases, a class diagram and interface mockups. These mockups are design references, not screenshots of the running application.

## Prototype limitations

Authentication compares passwords directly against database values, and API sessions are stored in memory. Use synthetic local data only. Password hashing, stronger validation and an authorization review are needed before adapting this prototype for real users.
