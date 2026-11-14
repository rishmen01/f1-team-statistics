/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.app;

import f1stats.io.CsvRaceDataRepository;
import f1stats.model.DriverRaceRecord;
import f1stats.model.TeamRaceSummary;
import f1stats.service.RaceAnalysisService;
import f1stats.ui.ConsoleInput;

import java.io.IOException;
import java.util.Scanner;

/** Loads driver results from CSV and provides all-race and car-code analysis. */
public class F1RaceAnalysisApp {
    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput(new Scanner(System.in));
        CsvRaceDataRepository repository = new CsvRaceDataRepository();
        RaceAnalysisService analysisService = new RaceAnalysisService();

        System.out.println("============================================");
        System.out.println("         F1 RACE ANALYSIS SYSTEM");
        System.out.println("============================================");

        DriverRaceRecord[] records = loadRecords(input, repository);
        if (records == null) {
            System.out.println("Analysis cancelled.");
            return;
        }
        if (records.length == 0) {
            System.out.println("The selected CSV file contains no driver records.");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\n1. Analyse all Grand Prix races");
            System.out.println("2. Find drivers by car code");
            System.out.println("Q. Quit");
            String choice = input.readMenuChoice("Choose an option: ");

            if ("1".equals(choice)) {
                try {
                    displayAllRaceAnalysis(records, analysisService);
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    System.out.println("Analysis could not be completed: " + exception.getMessage());
                }
            } else if ("2".equals(choice)) {
                displayCarCodeAnalysis(records, input, analysisService);
            } else if ("Q".equalsIgnoreCase(choice)) {
                exit = true;
            } else {
                System.out.println("Invalid option. Choose 1, 2 or Q.");
            }
        }
        System.out.println("Thank you for using the F1 Race Analysis System.");
    }

    private static DriverRaceRecord[] loadRecords(ConsoleInput input,
                                                   CsvRaceDataRepository repository) {
        while (true) {
            String fileName = input.readNonEmptyString(
                    "CSV file path (or type Q to quit): ");
            if ("Q".equalsIgnoreCase(fileName)) {
                return null;
            }
            try {
                DriverRaceRecord[] records = repository.readRecords(fileName);
                System.out.println("Loaded " + records.length + " driver race records.");
                return records;
            } catch (IOException exception) {
                System.out.println("Could not read the CSV file: " + exception.getMessage());
                System.out.println("Check the path and CSV format, then try again.");
            }
        }
    }

    private static void displayAllRaceAnalysis(DriverRaceRecord[] allRecords,
                                               RaceAnalysisService service) {
        String[] grandPrixNames = service.getGrandPrixNames(allRecords);
        for (String grandPrix : grandPrixNames) {
            DriverRaceRecord[] raceRecords = service.getRecordsForGrandPrix(allRecords, grandPrix);
            TeamRaceSummary[] summaries = service.getTeamSummaries(raceRecords);

            System.out.println("\n================================================");
            System.out.println("RACE ANALYSIS: " + grandPrix.toUpperCase());
            System.out.println("================================================");

            System.out.println("\nTeams with at least one driver completing the race:");
            boolean completedTeamFound = false;
            for (TeamRaceSummary summary : summaries) {
                if (summary.hasAtLeastOneFinisher()) {
                    System.out.println("- " + summary.getTeamName());
                    completedTeamFound = true;
                }
            }
            if (!completedTeamFound) {
                System.out.println("None.");
            }

            System.out.println("\nTeams with no driver completing the race:");
            boolean noFinisherFound = false;
            for (TeamRaceSummary summary : summaries) {
                if (summary.hasNoFinishers()) {
                    System.out.println("- " + summary.getTeamName());
                    noFinisherFound = true;
                }
            }
            if (!noFinisherFound) {
                System.out.println("None.");
            }

            TeamRaceSummary fastestTeam = service.getFastestTeam(summaries);
            if (fastestTeam != null) {
                System.out.printf("\nFastest team: %s (combined time: %.3f seconds)%n",
                        fastestTeam.getTeamName(), fastestTeam.getCombinedTime());
            }

            System.out.println("\nTeams by combined time (descending: highest time first):");
            TeamRaceSummary[] sortedTeams = service.sortTeamsByCombinedTimeDescending(summaries);
            for (TeamRaceSummary summary : sortedTeams) {
                System.out.printf("- %-22s %.3f seconds%n",
                        summary.getTeamName(), summary.getCombinedTime());
            }

            System.out.println("\nDrivers by fastest-lap time (descending: highest time first):");
            DriverRaceRecord[] descendingDrivers = service.sortDriversDescending(raceRecords);
            displayDrivers(descendingDrivers);

            System.out.println("\nDrivers by fastest-lap time (ascending: lowest time first):");
            DriverRaceRecord[] ascendingDrivers = service.sortDriversAscending(raceRecords);
            displayDrivers(ascendingDrivers);
        }
    }

    private static void displayCarCodeAnalysis(DriverRaceRecord[] records,
                                               ConsoleInput input,
                                               RaceAnalysisService service) {
        String carCode = input.readNonEmptyString("Enter the car code: ");
        String[] grandPrixNames = service.getGrandPrixNames(records);
        boolean found = false;

        for (String grandPrix : grandPrixNames) {
            DriverRaceRecord[] matchingDrivers = service.filterByCarCode(records, carCode, grandPrix);
            if (matchingDrivers.length > 0) {
                found = true;
                DriverRaceRecord[] sortedDrivers = service.sortDriversAscending(matchingDrivers);
                System.out.println("\n" + carCode.toUpperCase() + " - " + grandPrix);
                System.out.println("Drivers (fastest first):");
                displayDrivers(sortedDrivers);
            }
        }
        if (!found) {
            System.out.println("No driver records found for car code " + carCode + ".");
        }
    }

    private static void displayDrivers(DriverRaceRecord[] records) {
        if (records.length == 0) {
            System.out.println("No driver records found.");
            return;
        }
        for (DriverRaceRecord record : records) {
            String status = record.getPositionFinished() == -1 ? " (DNF)" : "";
            System.out.printf("- %-24s %-20s %.3f seconds%s%n",
                    record.getDriverName(), record.getTeamName(), record.getFastestLap(), status);
        }
    }
}
