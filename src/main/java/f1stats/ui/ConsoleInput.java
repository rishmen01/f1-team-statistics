/*
 * Development acknowledgement: AI assistance (ChatGPT) was used during this
 * portfolio refactor for code review, design suggestions, implementation of
 * validation/error handling, testing support, and documentation. Review and
 * verify this code before reuse or publication.
 */

package f1stats.ui;

import java.util.Scanner;

/** Centralises console input and validation so both applications behave consistently. */
public class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }
        this.scanner = scanner;
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    public int readPositiveInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(value);
                if (number > 0) {
                    return number;
                }
                System.out.println("Enter a whole number greater than zero.");
            } catch (NumberFormatException exception) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    /** Accepts -1 for DNF or a positive finishing position. */
    public int readFinishingPosition(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int position = Integer.parseInt(value);
                if (position == -1 || position > 0) {
                    return position;
                }
                System.out.println("Enter -1 for DNF or a positive finishing position.");
            } catch (NumberFormatException exception) {
                System.out.println("Invalid position. Enter a whole number, such as -1, 1, 2 or 3.");
            }
        }
    }

    public double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                double number = Double.parseDouble(value);
                if (!Double.isNaN(number) && !Double.isInfinite(number) && number >= 0.0) {
                    return number;
                }
                System.out.println("Enter a finite number greater than or equal to zero.");
            } catch (NumberFormatException exception) {
                System.out.println("Invalid time. Enter a number such as 78.813.");
            }
        }
    }

    public String readMenuChoice(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
