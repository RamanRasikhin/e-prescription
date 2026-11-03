# E-Prescription

A university team prototype for issuing prescriptions, viewing prescription history and dispensing medication.

Developed as a university team project; I was one of the contributors.

## Features

- JavaFX views for patients, doctors and pharmacists.
- Prescription creation, lookup and fulfillment.
- Medication inventory and database access objects.
- A separate NanoHTTPD server exposing selected workflows through HTTP.

## Stack

Java В· JavaFX В· SQLite/JDBC В· Gson В· NanoHTTPD

## Run

Requires JDK 17 and Maven:

```powershell
mvn test
mvn javafx:run
```

The desktop entry point is `Main`. The HTTP entry point is `ServerAPI`; it can be started separately with:

```powershell
mvn compile exec:java -Dexec.mainClass=ServerAPI
```

The server listens on port 8080 and creates demo records on startup. Run it with a fresh local database when evaluating it; repeated startup is not a migration or production seeding mechanism. The GUI and server are separate entry points, not a confirmed client/server integration.

The database file is `apteka.db` in the working directory. It is created automatically and is excluded from Git. Desktop demo logins are `lekarz`, `pacjent` and `farmaceuta`, with password `pass1`; HTTP demo logins are `doc1`, `pat1` and `apt1`, with the same demo password.

`mvn test` resolves the declared dependencies, compiles the application and runs eight integration tests against temporary SQLite databases. They cover issuing and viewing prescriptions, stock quantities, atomic fulfillment and rollback, expiry, duplicate fulfillment and role sessions. `OldMainTest` is a separate manual demo for a fresh database. Desktop screen construction and the HTTP login/issue/history/fulfillment flow are checked separately; the complete GUI has not been tested end to end.

## Design materials

`pliki niezwiazane z kodem/` contains use cases, a class diagram and interface mockups. These mockups are design references, not screenshots of the running application.

## Prototype limitations

Authentication compares passwords directly against database values, and API sessions are stored in memory. Use synthetic local data only. Password hashing, stronger validation and an authorization review are needed before adapting this prototype for real users.
