# Stephaneum JSF

Webplattform des Stephaneums

- Homepage
- Beiträge
- gesamte "interne Bereich":
    - Login
    - Cloud
    - Projekte
    - Backup
    - und vieles mehr

## Projekt starten

Folgende Schritte sind notwendig, um diese Software zum Laufen zu bringen.

### Voraussetzungen

- Java 8
- Tomcat 8.5
- MariaDB 10.1
- IntelliJ CE / UE (empfohlen)

### Installation des eigentlichen Projektes

1. Quellcode herunterladen
    - über git clone
    - oder über das manuelle Herunterladen der .zip
2. Ausführung in IntelliJ
    1. rechts den Gradle-Reiter öffnen
    2. Doppelklick: `Tasks` > `web application` > `tomcatRun`
    3. zum Stoppen: `Tasks` > `web application` > `tomcatStop`

## Export als .war-Datei

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: `Tasks` > `build` > `war`
3. die Datei liegt nun in `<Projekt-Ordner>/build/libs/`