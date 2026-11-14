/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats;

import f1stats.io.CsvRaceDataRepository;
import f1stats.model.DriverRaceRecord;
import f1stats.model.TeamRaceSummary;
import f1stats.service.RaceAnalysisService;

import java.io.File;
import java.io.IOException;

/** Small dependency-free test harness; run with java -cp out:test-out f1stats.F1StatisticsTest. */
public class F1StatisticsTest {
    private int passed;
    private int failed;

    public static void main(String[] args) {
        new F1StatisticsTest().runAll();
    }

    private void runAll() {
        testDriverRecordValidation();
        testTeamGroupingAndFinisherAnalysis();
        testDnfPenaltyAndFastestTeam();
        testTeamDescendingSort();
        testDriverAscendingAndDescendingSort();
        testCarCodeFilterReturnsBothDrivers();
        testIncompleteTeamGroupIsRejected();
        testDriverEqualityAndHashCode();
        testCsvRoundTrip();
        testCsvRoundTripWithCommaInTextField();
        testInvalidCsvHeaderIsRejected();
        System.out.println("\nTest results: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            throw new AssertionError("One or more tests failed.");
        }
    }

    private void testDriverRecordValidation() {
        DriverRaceRecord valid = record("Example GP", "Team A", "AAA", "Driver One", 1, 81.2);
        check("-1 is accepted as DNF", createRecordWithoutFailure(-1));
        check("zero finishing position is rejected", throwsIllegalArgument(() -> valid.setPositionFinished(0)));
        check("negative lap time is rejected", throwsIllegalArgument(() -> valid.setFastestLap(-0.1)));
        check("NaN lap time is rejected", throwsIllegalArgument(() -> valid.setFastestLap(Double.NaN)));
        check("empty driver name is rejected", throwsIllegalArgument(() -> valid.setDriverName(" ")));
    }

    private void testTeamGroupingAndFinisherAnalysis() {
        RaceAnalysisService service = new RaceAnalysisService();
        TeamRaceSummary[] summaries = service.getTeamSummaries(sampleRace());
        check("three teams are grouped from six driver records", summaries.length == 3);
        check("team with one finisher is classified as completed", summaries[0].hasAtLeastOneFinisher());
        check("team with two DNFs is classified as no finishers", summaries[2].hasNoFinishers());
    }

    private void testDnfPenaltyAndFastestTeam() {
        RaceAnalysisService service = new RaceAnalysisService();
        TeamRaceSummary[] summaries = service.getTeamSummaries(sampleRace());
        TeamRaceSummary redBull = findTeam(summaries, "Red Bull Racing");
        TeamRaceSummary ferrari = findTeam(summaries, "Ferrari");
        check("DNF driver contributes 205.50 seconds", closeTo(redBull.getCombinedTime(), 285.5));
        check("fastest team uses both drivers' times", closeTo(ferrari.getCombinedTime(), 160.0));
        check("fastest team is Ferrari", "Ferrari".equals(service.getFastestTeam(summaries).getTeamName()));
    }

    private void testTeamDescendingSort() {
        RaceAnalysisService service = new RaceAnalysisService();
        TeamRaceSummary[] sorted = service.sortTeamsByCombinedTimeDescending(
                service.getTeamSummaries(sampleRace()));
        check("team order is descending by combined seconds",
                sorted[0].getCombinedTime() >= sorted[1].getCombinedTime()
                        && sorted[1].getCombinedTime() >= sorted[2].getCombinedTime());
    }

    private void testDriverAscendingAndDescendingSort() {
        RaceAnalysisService service = new RaceAnalysisService();
        DriverRaceRecord[] race = sampleRace();
        DriverRaceRecord[] ascending = service.sortDriversAscending(race);
        DriverRaceRecord[] descending = service.sortDriversDescending(race);
        check("driver ascending sort puts the lowest lap first",
                ascending[0].getFastestLap() <= ascending[1].getFastestLap());
        check("driver descending sort puts the highest lap first",
                descending[0].getFastestLap() >= descending[1].getFastestLap());
    }

    private void testCarCodeFilterReturnsBothDrivers() {
        RaceAnalysisService service = new RaceAnalysisService();
        DriverRaceRecord[] filtered = service.filterByCarCode(sampleRace(), "SF-24", "Australian Grand Prix");
        check("car-code filter returns both Ferrari drivers", filtered.length == 2);
        DriverRaceRecord[] sorted = service.sortDriversAscending(filtered);
        check("filtered drivers are sorted fastest first",
                sorted[0].getFastestLap() <= sorted[1].getFastestLap());
    }

    private void testIncompleteTeamGroupIsRejected() {
        RaceAnalysisService service = new RaceAnalysisService();
        DriverRaceRecord[] incomplete = new DriverRaceRecord[] {
                record("Australian Grand Prix", "Team A", "AAA", "Driver One", 1, 80.0)
        };
        boolean rejected;
        try {
            service.getTeamSummaries(incomplete);
            rejected = false;
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check("incomplete one-driver team is rejected", rejected);
    }

    private void testDriverEqualityAndHashCode() {
        DriverRaceRecord first = record("Australian Grand Prix", "Ferrari", "SF-24",
                "Driver C", 2, 79.0);
        DriverRaceRecord second = record("Australian Grand Prix", "Ferrari", "SF-24",
                "Driver C", 2, 79.0);
        check("equal driver records compare as equal", first.equals(second));
        check("equal driver records have equal hash codes", first.hashCode() == second.hashCode());
    }

    private void testCsvRoundTrip() {
        File temporaryFile = null;
        try {
            temporaryFile = File.createTempFile("f1-statistics-test", ".csv");
            CsvRaceDataRepository repository = new CsvRaceDataRepository();
            DriverRaceRecord[] original = sampleRace();
            repository.writeRecords(temporaryFile.getAbsolutePath(), original);
            DriverRaceRecord[] loaded = repository.readRecords(temporaryFile.getAbsolutePath());
            boolean same = original.length == loaded.length;
            for (int i = 0; same && i < original.length; i++) {
                same = original[i].equals(loaded[i]);
            }
            check("CSV records can be written and read back", same);
        } catch (IOException exception) {
            check("CSV records can be written and read back", false);
        } finally {
            if (temporaryFile != null && temporaryFile.exists()) {
                temporaryFile.delete();
            }
        }
    }

    private void testCsvRoundTripWithCommaInTextField() {
        File temporaryFile = null;
        try {
            temporaryFile = File.createTempFile("f1-statistics-comma-test", ".csv");
            CsvRaceDataRepository repository = new CsvRaceDataRepository();
            DriverRaceRecord[] original = new DriverRaceRecord[] {
                    record("Australian Grand Prix", "Team, Racing", "TR-01", "Driver, One", 1, 80.0),
                    record("Australian Grand Prix", "Team, Racing", "TR-01", "Driver Two", 2, 81.0)
            };
            repository.writeRecords(temporaryFile.getAbsolutePath(), original);
            DriverRaceRecord[] loaded = repository.readRecords(temporaryFile.getAbsolutePath());
            check("CSV quoting preserves commas inside text fields",
                    loaded.length == 2 && original[0].equals(loaded[0]) && original[1].equals(loaded[1]));
        } catch (IOException exception) {
            check("CSV quoting preserves commas inside text fields", false);
        } finally {
            if (temporaryFile != null && temporaryFile.exists()) {
                temporaryFile.delete();
            }
        }
    }

    private void testInvalidCsvHeaderIsRejected() {
        File temporaryFile = null;
        boolean rejected = false;
        try {
            temporaryFile = File.createTempFile("f1-statistics-invalid-header", ".csv");
            java.io.PrintWriter writer = new java.io.PrintWriter(temporaryFile, "UTF-8");
            writer.println("Name,Car,Driver,Race,Position,Lap");
            writer.close();
            new CsvRaceDataRepository().readRecords(temporaryFile.getAbsolutePath());
        } catch (IOException exception) {
            rejected = true;
        } finally {
            if (temporaryFile != null && temporaryFile.exists()) {
                temporaryFile.delete();
            }
        }
        check("invalid CSV header is rejected", rejected);
    }

    private DriverRaceRecord[] sampleRace() {
        return new DriverRaceRecord[] {
                record("Australian Grand Prix", "Red Bull Racing", "RB20", "Driver A", 1, 80.0),
                record("Australian Grand Prix", "Red Bull Racing", "RB20", "Driver B", -1, 85.0),
                record("Australian Grand Prix", "Ferrari", "SF-24", "Driver C", 2, 79.0),
                record("Australian Grand Prix", "Ferrari", "SF-24", "Driver D", 3, 81.0),
                record("Australian Grand Prix", "Mercedes", "W15", "Driver E", -1, 90.0),
                record("Australian Grand Prix", "Mercedes", "W15", "Driver F", -1, 85.0)
        };
    }

    private DriverRaceRecord record(String grandPrix, String team, String carCode,
                                    String driver, int position, double lap) {
        return new DriverRaceRecord(team, carCode, driver, grandPrix, position, lap);
    }

    private TeamRaceSummary findTeam(TeamRaceSummary[] summaries, String teamName) {
        for (TeamRaceSummary summary : summaries) {
            if (summary.getTeamName().equals(teamName)) {
                return summary;
            }
        }
        throw new IllegalArgumentException("Team not found: " + teamName);
    }

    private boolean createRecordWithoutFailure(int position) {
        try {
            record("Example GP", "Team A", "AAA", "Driver One", position, 81.2);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }

    private boolean closeTo(double left, double right) {
        return Math.abs(left - right) < 0.000001;
    }

    private void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + description);
        } else {
            failed++;
            System.out.println("FAIL: " + description);
        }
    }
}
