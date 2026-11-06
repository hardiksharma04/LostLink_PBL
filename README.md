# LostLink

LostLink is a JavaFX and SQLite desktop application for reporting and matching lost and found items. It is designed as a Java Programming Lab project and separates the interface, data model, database access, account/report services, and matching logic into packages.

## Requirements

- JDK 21 or newer (a JDK, not only a JRE)
- Maven 3.9 or newer
- Internet access the first time Maven downloads JavaFX and SQLite JDBC

## Run

From this folder, after installing Maven and a JDK 21+:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn javafx:run
```

The `JAVA_HOME` path above matches the JDK found on the development computer. If your JDK is installed elsewhere, use its directory instead. The app is configured for JDK 21; the `java` command currently found first on that computer's PATH is Java 8.

On the first launch, create the administrator account using a password of at least 12 characters. There is no shared or preconfigured admin password. Then register standard user accounts from the sign-in screen.

## Features

- Local account creation and sign-in; passwords are stored as salted PBKDF2 hashes.
- Create, browse, search, and filter lost/found reports.
- Edit or delete your own reports; admins can moderate all reports.
- Admin verification and Open / Matched / Returned status controls.
- Weighted possible matches based on category, colour, location, date proximity, and shared descriptive words.
- SQLite storage, initialized on first launch, at `%USERPROFILE%\.lostlink\lostlink.db`.

## Important deployment scope

This version is a standalone app with a separate SQLite database on each computer. It does **not** synchronize reports between students' computers and is not a shared campus service. The database is not encrypted at rest; use it only on a trusted, OS-protected computer. Avoid placing student ID numbers or other highly sensitive data in descriptions. Contact details are kept out of the browse table and shown only with possible matches.

To serve the whole campus, the project needs a separately designed and secured shared backend, user identity policy, deployment/backup plan, and privacy review. The current local desktop app is suitable for a lab demonstration or a single shared computer.

## Project map

- `model`: `User` and `ItemReport` domain objects
- `db`: SQLite connection and schema initialization
- `service`: authentication, report CRUD, and persistence operations
- `matching`: match-engine facade
- `ui`: JavaFX screens and event handling
- `docs/PROJECT_REPORT.md`: design notes, schema, scoring, and limitations

## Troubleshooting

- `mvn` is not recognized: install Maven and reopen PowerShell.
- Java version is below 21: install a JDK 21+ and confirm `java -version` and `mvn -version` use it.
- Dependency download fails: check network access, then retry `mvn javafx:run`.
- To reset a demonstration database, close LostLink and remove `%USERPROFILE%\.lostlink\lostlink.db`. This permanently removes local accounts and reports.
