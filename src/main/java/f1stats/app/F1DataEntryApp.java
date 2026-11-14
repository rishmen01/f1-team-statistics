/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.app;

import f1stats.io.CsvRaceDataRepository;
import f1stats.model.DriverRaceRecord;
import f1stats.ui.ConsoleInput;

import java.io.IOException;
import java.util.Scanner;

/** Collects driver results for each team and Grand Prix and writes them to CSV. */
public class F1DataEntryApp {
    private static final int MAX_RECORDS = 10000;

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput(new Scanner(System.in));
        CsvRaceDataRepository repository = new CsvRaceDataRepository();

        System.out.println("============================================");
        System.out.println("       F1 TEAM DATA ENTRY SYSTEM");
        System.out.println("============================================");
        System.out.println("Each driver has one record for each Grand Prix.");
        System.out.println("A team has two drivers. Enter -1 for a DNF position.\n");

        int numberOfTeams = input.readPositiveInteger("Number of F1 teams: ");
        int numberOfRaces = input.readPositiveInteger("Number of Grand Prix races: ");
        long requiredRecords = (long) numberOfTeams * numberOfRaces * 2L;
        if (requiredRecords > MAX_RECORDS) {
            System.out.println("The requested data set is too large. Maximum records: " + MAX_RECORDS);
            return;
        }

        int totalRecords = (int) requiredRecords;
        String[] grandPrixNames = new String[numberOfRaces];
        for (int raceIndex = 0; raceIndex < numberOfRaces; raceIndex++) {
            while (true) {
                String raceName = input.readNonEmptyString(
                        "Name of Grand Prix " + (raceIndex + 1) + ": ");
                if (!containsIgnoreCase(grandPrixNames, raceIndex, raceName)) {
                    grandPrixNames[raceIndex] = raceName;
                    break;
                }
                System.out.println("That Grand Prix has already been entered. Enter a different race.");
            }
        }

        DriverRaceRecord[] records = new DriverRaceRecord[totalRecords];
        int recordIndex = 0;

        for (int teamIndex = 0; teamIndex < numberOfTeams; teamIndex++) {
            System.out.println("\n--- Team " + (teamIndex + 1) + " of " + numberOfTeams + " ---");
            String teamName = input.readNonEmptyString("Team name: ");
            String carCode = input.readNonEmptyString("Car code: ");
            String[] driverNames = new String[2];
            driverNames[0] = input.readNonEmptyString("Driver 1 name: ");
            while (true) {
                driverNames[1] = input.readNonEmptyString("Driver 2 name: ");
                if (!driverNames[0].equalsIgnoreCase(driverNames[1])) {
                    break;
                }
                System.out.println("A team must have two different driver names. Please enter Driver 2 again.");
            }

            for (int raceIndex = 0; raceIndex < numberOfRaces; raceIndex++) {
                System.out.println("\n" + teamName + " - " + grandPrixNames[raceIndex]);
                for (int driverIndex = 0; driverIndex < 2; driverIndex++) {
                    System.out.println("Results for " + driverNames[driverIndex] + ":");
                    int position = input.readFinishingPosition("Position finished (-1 for DNF): ");
                    double fastestLap = input.readNonNegativeDouble("Fastest lap (seconds): ");
                    records[recordIndex] = new DriverRaceRecord(teamName, carCode,
                            driverNames[driverIndex], grandPrixNames[raceIndex], position, fastestLap);
                    recordIndex++;
                }
            }
        }

        String outputName = input.readNonEmptyString("Output CSV file name (with or without .csv): ");
        if (!outputName.toLowerCase().endsWith(".csv")) {
            outputName += ".csv";
        }

        try {
            repository.writeRecords(outputName, records);
            System.out.println("\nSuccessfully saved " + records.length + " driver race records to " + outputName + ".");
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Could not save the CSV file: " + exception.getMessage());
        }
    }

    private static boolean containsIgnoreCase(String[] values, int count, String target) {
        for (int i = 0; i < count; i++) {
            if (values[i].equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
