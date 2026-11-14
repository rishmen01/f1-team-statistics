/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.io;

import f1stats.model.DriverRaceRecord;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Handles CSV persistence for both F1 applications (single responsibility). */
public class CsvRaceDataRepository {
    public static final String HEADER =
            "TeamName,CarCode,DriverName,GrandPrix,PositionFinished,FastestLap";

    /** Writes the provided records using PrintWriter and UTF-8 CSV output. */
    public void writeRecords(String fileName, DriverRaceRecord[] records) throws IOException {
        if (records == null) {
            throw new IllegalArgumentException("Records array cannot be null.");
        }
        Path path = Paths.get(fileName);
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            throw new IOException("Directory does not exist: " + parent);
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(path.toFile()), StandardCharsets.UTF_8))) {
            writer.println(HEADER);
            for (DriverRaceRecord record : records) {
                if (record == null) {
                    throw new IllegalArgumentException("Records array contains an empty entry.");
                }
                writer.println(quote(record.getTeamName()) + ","
                        + quote(record.getCarCode()) + ","
                        + quote(record.getDriverName()) + ","
                        + quote(record.getGrandPrix()) + ","
                        + record.getPositionFinished() + ","
                        + Double.toString(record.getFastestLap()));
            }
            writer.flush();
            if (writer.checkError()) {
                throw new IOException("An error occurred while writing " + fileName + ".");
            }
        }
    }

    /**
     * Reads a CSV file into a fixed-size array. The file is counted first so
     * the required array can be allocated without using ArrayList.
     */
    public DriverRaceRecord[] readRecords(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IOException("File does not exist or cannot be read: " + fileName);
        }

        int recordCount = countRecords(path);
        DriverRaceRecord[] records = new DriverRaceRecord[recordCount];

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            validateHeader(line, fileName);
            int index = 0;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (index >= records.length) {
                    throw new IOException("The CSV file changed while it was being read.");
                }
                records[index] = parseRecord(line, lineNumber);
                index++;
            }
            if (index != records.length) {
                DriverRaceRecord[] actualRecords = new DriverRaceRecord[index];
                for (int i = 0; i < index; i++) {
                    actualRecords[i] = records[i];
                }
                return actualRecords;
            }
        }
        return records;
    }

    private int countRecords(Path path) throws IOException {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            validateHeader(header, path.toString());
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void validateHeader(String header, String fileName) throws IOException {
        if (header == null) {
            throw new IOException("The CSV file is empty: " + fileName);
        }
        String normalised = header.replace("\uFEFF", "").replace(" ", "").trim();
        if (!HEADER.equalsIgnoreCase(normalised)) {
            throw new IOException("Unexpected CSV header in " + fileName + ". Expected: " + HEADER);
        }
    }

    private DriverRaceRecord parseRecord(String line, int lineNumber) throws IOException {
        String[] fields = parseCsvLine(line, lineNumber);
        if (fields.length != 6) {
            throw new IOException("Line " + lineNumber + " must contain exactly six CSV fields.");
        }
        try {
            String teamName = fields[0].trim();
            String carCode = fields[1].trim();
            String driverName = fields[2].trim();
            String grandPrix = fields[3].trim();
            int position = Integer.parseInt(fields[4].trim());
            double fastestLap = Double.parseDouble(fields[5].trim());
            return new DriverRaceRecord(teamName, carCode, driverName, grandPrix,
                    position, fastestLap);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid data on CSV line " + lineNumber + ": "
                    + exception.getMessage(), exception);
        }
    }

    /** Parses a single CSV row, including quoted fields and escaped double quotes. */
    private String[] parseCsvLine(String line, int lineNumber) throws IOException {
        String[] fields = new String[6];
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        int fieldIndex = 0;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                if (fieldIndex >= fields.length) {
                    throw new IOException("Too many CSV fields on line " + lineNumber + ".");
                }
                fields[fieldIndex] = current.toString();
                current.setLength(0);
                fieldIndex++;
            } else {
                current.append(character);
            }
        }

        if (insideQuotes) {
            throw new IOException("Unclosed quoted field on CSV line " + lineNumber + ".");
        }
        if (fieldIndex >= fields.length) {
            throw new IOException("Too many CSV fields on line " + lineNumber + ".");
        }
        fields[fieldIndex] = current.toString();

        if (fieldIndex != 5) {
            throw new IOException("Line " + lineNumber + " must contain exactly six CSV fields.");
        }
        return fields;
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
