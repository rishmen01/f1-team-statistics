/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.model;

import java.util.Objects;

/**
 * Represents one driver's result for one Formula One Grand Prix.
 *
 * A separate object is created for each driver at each race. This keeps the
 * six fields required by the COMP1007 specification together in one reusable
 * model class that can be shared by both applications.
 */
public final class DriverRaceRecord {
    private String teamName;
    private String carCode;
    private String driverName;
    private String grandPrix;
    private int positionFinished;
    private double fastestLap;

    public DriverRaceRecord(String teamName, String carCode, String driverName,
                            String grandPrix, int positionFinished, double fastestLap) {
        setTeamName(teamName);
        setCarCode(carCode);
        setDriverName(driverName);
        setGrandPrix(grandPrix);
        setPositionFinished(positionFinished);
        setFastestLap(fastestLap);
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = requireText(teamName, "Team name");
    }

    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = requireText(carCode, "Car code");
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = requireText(driverName, "Driver name");
    }

    public String getGrandPrix() {
        return grandPrix;
    }

    public void setGrandPrix(String grandPrix) {
        this.grandPrix = requireText(grandPrix, "Grand Prix");
    }

    public int getPositionFinished() {
        return positionFinished;
    }

    /**
     * The assignment uses -1 to represent Did Not Finish (DNF).
     * A classified finishing position must be a positive integer.
     */
    public void setPositionFinished(int positionFinished) {
        if (positionFinished == 0 || positionFinished < -1) {
            throw new IllegalArgumentException(
                    "Position must be -1 for DNF or a positive finishing position.");
        }
        this.positionFinished = positionFinished;
    }

    public double getFastestLap() {
        return fastestLap;
    }

    public void setFastestLap(double fastestLap) {
        if (Double.isNaN(fastestLap) || Double.isInfinite(fastestLap) || fastestLap < 0.0) {
            throw new IllegalArgumentException("Fastest lap must be a finite, non-negative number.");
        }
        this.fastestLap = fastestLap;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    /** Returns the six values in a readable form for console output and debugging. */
    @Override
    public String toString() {
        return "DriverRaceRecord{" +
                "teamName='" + teamName + '\'' +
                ", carCode='" + carCode + '\'' +
                ", driverName='" + driverName + '\'' +
                ", grandPrix='" + grandPrix + '\'' +
                ", positionFinished=" + positionFinished +
                ", fastestLap=" + fastestLap +
                '}';
    }

    /** Equality is based on all six values in the race-result record. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DriverRaceRecord that = (DriverRaceRecord) other;
        return positionFinished == that.positionFinished
                && Double.compare(that.fastestLap, fastestLap) == 0
                && teamName.equals(that.teamName)
                && carCode.equals(that.carCode)
                && driverName.equals(that.driverName)
                && grandPrix.equals(that.grandPrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, carCode, driverName, grandPrix,
                positionFinished, fastestLap);
    }
}
