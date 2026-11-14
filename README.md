# F1 Team Statistics and Race Analysis

**Language:** Java  
**Project type:** Console-based data entry, CSV persistence and race analysis

A Java application for recording Formula One driver results and analysing team and driver performance across Grand Prix races. The assignment's data model represents one driver at one Grand Prix per record. A team therefore has two driver records for each race.

## Applications

### 1. `F1DataEntryApp` – F1 Team Data Entry System

- Prompts for the number of teams and Grand Prix races.
- Collects each team's name, car code and two driver names.
- Collects each driver's finishing position and fastest lap for every race.
- Accepts `-1` as the DNF position; otherwise, the finishing position must be positive.
- Validates non-empty text, integer positions and finite, non-negative lap times.
- Stores one `DriverRaceRecord` object per driver per Grand Prix in an array.
- Writes the records to a CSV file using `PrintWriter` and UTF-8.

### 2. `F1RaceAnalysisApp` – F1 Race Analysis System

- Loads driver records from a CSV file into an array of `DriverRaceRecord` objects.
- Re-prompts when the file path is wrong or the CSV is malformed.
- Analyses each Grand Prix separately.
- Lists teams with at least one driver finishing the race and teams where both drivers recorded DNF.
- Calculates combined team time using both drivers' fastest laps. A DNF contributes `205.50` seconds, as specified in the assignment.
- Displays the fastest team.
- Sorts teams by combined time in numeric descending order (highest time first).
- Sorts drivers by fastest-lap time in both descending and ascending order.
- Filters drivers by car code and displays matching drivers fastest first.

## Object-oriented design

The implementation uses object-oriented principles where they make the solution clearer, without adding unnecessary design patterns:

| Class | Responsibility |
|---|---|
| `DriverRaceRecord` | Shared model for the six required fields; encapsulation, validation, getters/setters, `toString()`, `equals()` and `hashCode()` |
| `TeamRaceSummary` | Composes two `DriverRaceRecord` objects and calculates the combined team time |
| `CsvRaceDataRepository` | Reads and writes CSV records; shared by both applications |
| `RaceAnalysisService` | Performs grouping, team calculations, filtering and array-based sorting |
| `ConsoleInput` | Centralises console input and validation |
| `F1DataEntryApp` | Entry point for data collection and CSV creation |
| `F1RaceAnalysisApp` | Entry point for CSV retrieval and analysis menus |

### Design concepts applied

- **Encapsulation:** model fields are private and updated through validated setters.
- **Single Responsibility Principle:** console input, CSV persistence, domain models and analysis logic are separated into focused classes.
- **Composition:** `TeamRaceSummary` contains the two driver-result objects needed for one team's race result.
- **Reusability and low coupling:** both applications use the same `DriverRaceRecord` and `CsvRaceDataRepository` classes.
- **Cohesion:** race calculation and sorting logic are kept in `RaceAnalysisService`, rather than being mixed into the menu code.

## Project structure

```text
f1-team-statistics/
├── README.md
├── .gitignore
├── data/
│   └── sample-f1-results.csv
├── pseudocode/
│   ├── ClassDesign.txt
│   ├── ProgramOne_DataEntry.txt
│   └── ProgramTwo_RaceAnalysis.txt
└── src/
    ├── main/java/f1stats/
    │   ├── app/
    │   │   ├── F1DataEntryApp.java
    │   │   └── F1RaceAnalysisApp.java
    │   ├── io/
    │   │   └── CsvRaceDataRepository.java
    │   ├── model/
    │   │   ├── DriverRaceRecord.java
    │   │   └── TeamRaceSummary.java
    │   ├── service/
    │   │   └── RaceAnalysisService.java
    │   └── ui/
    │       └── ConsoleInput.java
    └── test/java/f1stats/
        └── F1StatisticsTest.java
```

## Requirements

- JDK 8 or later
- No third-party libraries or build tools are required

## Compile and run

Run the commands from the repository root.

### Linux / macOS / Ubuntu

Compile the main source files:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
```

Start the data-entry application:

```bash
java -cp out f1stats.app.F1DataEntryApp
```

Start the analysis application:

```bash
java -cp out f1stats.app.F1RaceAnalysisApp
```

When the analysis application asks for a CSV path, you can use the included example:

```text
data/sample-f1-results.csv
```

### Windows PowerShell

Compile the main source files:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sourceFiles = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sourceFiles
```

Start the data-entry application:

```powershell
java -cp out f1stats.app.F1DataEntryApp
```

Start the analysis application:

```powershell
java -cp out f1stats.app.F1RaceAnalysisApp
```

## Run the test harness

The repository includes a dependency-free test harness covering input validation, DNF calculations, team grouping, sorting, car-code filtering and CSV round-tripping.

### Linux / macOS / Ubuntu

```bash
mkdir -p test-out
javac -encoding UTF-8 -cp out -d test-out $(find src/test/java -name "*.java")
java -cp out:test-out f1stats.F1StatisticsTest
```

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force test-out | Out-Null
$testFiles = Get-ChildItem -Recurse src/test/java -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp out -d test-out $testFiles
java -cp "out;test-out" f1stats.F1StatisticsTest
```

Expected result: **22 tests passed, 0 failed**.

## CSV format

The CSV file has the six columns required by the assignment:

```csv
TeamName,CarCode,DriverName,GrandPrix,PositionFinished,FastestLap
"Red Bull Racing","RB20","Driver A","Australian Grand Prix",1,80.0
"Red Bull Racing","RB20","Driver B","Australian Grand Prix",-1,85.0
```

- One row represents one driver's result in one Grand Prix.
- `PositionFinished = -1` represents Did Not Finish (DNF).
- A valid team/race grouping must contain exactly two different driver records.
- The CSV reader supports quoted fields and escaped double quotes.

The included `sample-f1-results.csv` uses **illustrative sample values**, not a claim of official 2024 race results.

## Important calculation and ordering notes

- The fastest team is the team with the **lowest combined time**.
- For the team calculation only, each DNF driver contributes `205.50` seconds instead of their recorded fastest lap.
- Team sorting follows numeric descending order of combined seconds, so the largest combined time is displayed first. The fastest team is identified separately.
- Driver sorting uses each driver's recorded fastest lap. Ascending order displays the lowest lap time first; descending order displays the highest lap time first.
- Analysis is performed separately for each Grand Prix so that results from different races are not combined.

## Validation and error handling

- Required text fields cannot be blank.
- Finishing position accepts `-1` or a positive integer; zero and values below `-1` are rejected.
- Fastest lap must be a finite, non-negative number.
- Invalid numeric console input is rejected and re-prompted rather than terminating the application.
- Missing files, invalid headers, malformed rows and invalid numeric CSV values produce a helpful error message.
- Incomplete team/race groups and duplicate or excess driver records are reported rather than silently treated as valid two-driver teams.

## Pseudocode

The `pseudocode/` directory contains:

- `ClassDesign.txt` – class fields, constructor, validation, getters/setters and object overrides.
- `ProgramOne_DataEntry.txt` – data-entry and CSV-storage workflow.
- `ProgramTwo_RaceAnalysis.txt` – CSV retrieval, team grouping and analysis workflow.



