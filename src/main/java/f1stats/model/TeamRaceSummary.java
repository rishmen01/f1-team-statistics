/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.model;

/**
 * Combines the two driver records belonging to one team in one Grand Prix.
 * This is a derived analysis object; the CSV still stores one row per driver.
 */
public class TeamRaceSummary {
    public static final double DNF_TIME_PENALTY = 205.50;

    private final String teamName;
    private final String carCode;
    private final String grandPrix;
    private DriverRaceRecord firstDriver;
    private DriverRaceRecord secondDriver;

    public TeamRaceSummary(String teamName, String carCode, String grandPrix) {
        this.teamName = teamName;
        this.carCode = carCode;
        this.grandPrix = grandPrix;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getCarCode() {
        return carCode;
    }

    public String getGrandPrix() {
        return grandPrix;
    }

    public DriverRaceRecord getFirstDriver() {
        return firstDriver;
    }

    public DriverRaceRecord getSecondDriver() {
        return secondDriver;
    }

    public int getDriverCount() {
        int count = 0;
        if (firstDriver != null) {
            count++;
        }
        if (secondDriver != null) {
            count++;
        }
        return count;
    }

    /** Adds a driver record, rejecting duplicate or third drivers for a team/race. */
    public void addDriver(DriverRaceRecord driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver record cannot be null.");
        }
        if (firstDriver != null && sameDriver(firstDriver, driver)) {
            throw new IllegalArgumentException("Duplicate driver record for " + driver.getDriverName()
                    + " at " + grandPrix + ".");
        }
        if (secondDriver != null && sameDriver(secondDriver, driver)) {
            throw new IllegalArgumentException("Duplicate driver record for " + driver.getDriverName()
                    + " at " + grandPrix + ".");
        }
        if (firstDriver == null) {
            firstDriver = driver;
        } else if (secondDriver == null) {
            secondDriver = driver;
        } else {
            throw new IllegalArgumentException("More than two drivers were supplied for "
                    + teamName + " at " + grandPrix + ".");
        }
    }

    public boolean hasTwoDrivers() {
        return firstDriver != null && secondDriver != null;
    }

    public boolean hasAtLeastOneFinisher() {
        ensureComplete();
        return firstDriver.getPositionFinished() != -1
                || secondDriver.getPositionFinished() != -1;
    }

    public boolean hasNoFinishers() {
        ensureComplete();
        return firstDriver.getPositionFinished() == -1
                && secondDriver.getPositionFinished() == -1;
    }

    /**
     * Calculates the combined team time. A DNF driver contributes 205.50
     * seconds, exactly as specified in the COMP1007 assignment.
     */
    public double getCombinedTime() {
        ensureComplete();
        return adjustedTime(firstDriver) + adjustedTime(secondDriver);
    }

    private double adjustedTime(DriverRaceRecord driver) {
        if (driver.getPositionFinished() == -1) {
            return DNF_TIME_PENALTY;
        }
        return driver.getFastestLap();
    }

    private void ensureComplete() {
        if (!hasTwoDrivers()) {
            throw new IllegalStateException("Team " + teamName + " at " + grandPrix
                    + " must have exactly two driver records; found " + getDriverCount() + ".");
        }
    }

    private boolean sameDriver(DriverRaceRecord left, DriverRaceRecord right) {
        return left.getDriverName().equalsIgnoreCase(right.getDriverName());
    }

    @Override
    public String toString() {
        if (!hasTwoDrivers()) {
            return teamName + " (incomplete driver records)";
        }
        return teamName + " - " + String.format("%.3f seconds", getCombinedTime());
    }
}
