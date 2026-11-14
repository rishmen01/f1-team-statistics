/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.service;

import f1stats.model.DriverRaceRecord;
import f1stats.model.TeamRaceSummary;

/** Contains the race-analysis rules, separate from console input/output. */
public class RaceAnalysisService {

    /** Returns the distinct Grand Prix names in first-seen order. */
    public String[] getGrandPrixNames(DriverRaceRecord[] records) {
        if (records == null || records.length == 0) {
            return new String[0];
        }
        String[] names = new String[records.length];
        int count = 0;
        for (DriverRaceRecord record : records) {
            if (record != null && !containsIgnoreCase(names, count, record.getGrandPrix())) {
                names[count] = record.getGrandPrix();
                count++;
            }
        }
        return copyStrings(names, count);
    }

    public DriverRaceRecord[] getRecordsForGrandPrix(DriverRaceRecord[] records, String grandPrix) {
        int count = 0;
        for (DriverRaceRecord record : records) {
            if (record.getGrandPrix().equalsIgnoreCase(grandPrix)) {
                count++;
            }
        }
        DriverRaceRecord[] result = new DriverRaceRecord[count];
        int index = 0;
        for (DriverRaceRecord record : records) {
            if (record.getGrandPrix().equalsIgnoreCase(grandPrix)) {
                result[index] = record;
                index++;
            }
        }
        return result;
    }

    /** Groups the two driver records for each team in one Grand Prix. */
    public TeamRaceSummary[] getTeamSummaries(DriverRaceRecord[] raceRecords) {
        if (raceRecords == null || raceRecords.length == 0) {
            return new TeamRaceSummary[0];
        }
        TeamRaceSummary[] summaries = new TeamRaceSummary[raceRecords.length];
        int summaryCount = 0;

        for (DriverRaceRecord record : raceRecords) {
            int summaryIndex = findSummary(summaries, summaryCount, record);
            if (summaryIndex == -1) {
                summaries[summaryCount] = new TeamRaceSummary(record.getTeamName(),
                        record.getCarCode(), record.getGrandPrix());
                summaryIndex = summaryCount;
                summaryCount++;
            }
            summaries[summaryIndex].addDriver(record);
        }

        TeamRaceSummary[] result = new TeamRaceSummary[summaryCount];
        for (int i = 0; i < summaryCount; i++) {
            if (!summaries[i].hasTwoDrivers()) {
                throw new IllegalArgumentException("Incomplete data: " + summaries[i].getTeamName()
                        + " at " + summaries[i].getGrandPrix() + " has only one driver record.");
            }
            result[i] = summaries[i];
        }
        return result;
    }

    public TeamRaceSummary getFastestTeam(TeamRaceSummary[] summaries) {
        if (summaries == null || summaries.length == 0) {
            return null;
        }
        TeamRaceSummary fastest = summaries[0];
        for (int i = 1; i < summaries.length; i++) {
            if (summaries[i].getCombinedTime() < fastest.getCombinedTime()) {
                fastest = summaries[i];
            }
        }
        return fastest;
    }

    /** Sorts team summaries by combined time in numeric descending order. */
    public TeamRaceSummary[] sortTeamsByCombinedTimeDescending(TeamRaceSummary[] summaries) {
        TeamRaceSummary[] sorted = copySummaries(summaries);
        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - i - 1; j++) {
                if (sorted[j].getCombinedTime() < sorted[j + 1].getCombinedTime()) {
                    TeamRaceSummary temporary = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = temporary;
                }
            }
        }
        return sorted;
    }

    /** Sorts driver records by fastest-lap time in ascending order (fastest first). */
    public DriverRaceRecord[] sortDriversAscending(DriverRaceRecord[] records) {
        DriverRaceRecord[] sorted = copyRecords(records);
        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - i - 1; j++) {
                if (sorted[j].getFastestLap() > sorted[j + 1].getFastestLap()) {
                    DriverRaceRecord temporary = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = temporary;
                }
            }
        }
        return sorted;
    }

    /** Sorts driver records by fastest-lap time in descending order (slowest first). */
    public DriverRaceRecord[] sortDriversDescending(DriverRaceRecord[] records) {
        DriverRaceRecord[] sorted = copyRecords(records);
        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - i - 1; j++) {
                if (sorted[j].getFastestLap() < sorted[j + 1].getFastestLap()) {
                    DriverRaceRecord temporary = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = temporary;
                }
            }
        }
        return sorted;
    }

    /** Filters by Car Code and Grand Prix; the caller can sort the result. */
    public DriverRaceRecord[] filterByCarCode(DriverRaceRecord[] records, String carCode,
                                               String grandPrix) {
        int count = 0;
        for (DriverRaceRecord record : records) {
            if (record.getCarCode().equalsIgnoreCase(carCode)
                    && record.getGrandPrix().equalsIgnoreCase(grandPrix)) {
                count++;
            }
        }
        DriverRaceRecord[] filtered = new DriverRaceRecord[count];
        int index = 0;
        for (DriverRaceRecord record : records) {
            if (record.getCarCode().equalsIgnoreCase(carCode)
                    && record.getGrandPrix().equalsIgnoreCase(grandPrix)) {
                filtered[index] = record;
                index++;
            }
        }
        return filtered;
    }

    private int findSummary(TeamRaceSummary[] summaries, int count, DriverRaceRecord record) {
        for (int i = 0; i < count; i++) {
            TeamRaceSummary summary = summaries[i];
            if (summary.getTeamName().equalsIgnoreCase(record.getTeamName())
                    && summary.getCarCode().equalsIgnoreCase(record.getCarCode())
                    && summary.getGrandPrix().equalsIgnoreCase(record.getGrandPrix())) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsIgnoreCase(String[] values, int count, String target) {
        for (int i = 0; i < count; i++) {
            if (values[i].equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private String[] copyStrings(String[] original, int count) {
        String[] copy = new String[count];
        for (int i = 0; i < count; i++) {
            copy[i] = original[i];
        }
        return copy;
    }

    private DriverRaceRecord[] copyRecords(DriverRaceRecord[] original) {
        DriverRaceRecord[] copy = new DriverRaceRecord[original.length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i];
        }
        return copy;
    }

    private TeamRaceSummary[] copySummaries(TeamRaceSummary[] original) {
        TeamRaceSummary[] copy = new TeamRaceSummary[original.length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i];
        }
        return copy;
    }
}
