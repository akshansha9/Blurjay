package com.packages;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ExcelReader {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\DELL\\Desktop\\New folder (2)\\Assignment_Timecard.xlsx"; // Replace with the actual file path

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Assuming the data is in the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, EmployeeData> employeeDataMap = new HashMap<>();

            // Iterate through rows and collect employee data
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip header row
                }

                // Add this print statement to display the row ID
                System.out.println("Processing row with ID: " + row.getRowNum());

                String timeIn = parseTime(row, 2);
                String timeOut = parseTime(row, 3);

                // Print the time values to help identify the issue
                System.out.println("timeIn: " + timeIn);
                System.out.println("timeOut: " + timeOut);

                // Provide default values if timeIn or timeOut is null
                timeIn = (timeIn != null) ? timeIn : "00:00"; // Change the default value as needed
                timeOut = (timeOut != null) ? timeOut : "00:00"; // Change the default value as needed

                // Add this print statement
                System.out.println("Processing valid time values for row: " + row.getRowNum());

                try {
                    LocalTime parsedTimeIn = LocalTime.parse(timeIn, DateTimeFormatter.ofPattern("H:mm"));
                    LocalTime parsedTimeOut = LocalTime.parse(timeOut, DateTimeFormatter.ofPattern("H:mm"));

                    String employeeName = row.getCell(7).getStringCellValue();
                    String position = row.getCell(0).getStringCellValue();

                    // Assuming Timecard Hours is a string with format "HH:mm"
                    LocalTime timecardHours = LocalTime.parse(row.getCell(4).getStringCellValue());
                    LocalDate payCycleStartDate = LocalDate.parse(row.getCell(5).getStringCellValue());
                    LocalDate payCycleEndDate = LocalDate.parse(row.getCell(6).getStringCellValue());

                    EmployeeData employeeData = employeeDataMap.computeIfAbsent(employeeName, k -> new EmployeeData());
                    employeeData.addWorkDay(position, parsedTimeIn, parsedTimeOut, timecardHours, payCycleStartDate, payCycleEndDate);
                } catch (Exception e) {
                    // Skip row with invalid time values
                    // Add this print statement
                    System.out.println("Error processing row: " + row.getRowNum());
                    e.printStackTrace();
                    continue;
                }
            }

            // Analyze and print results
            analyzeAndPrintResults(employeeDataMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseTime(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
            return "00:00"; // Default value for non-string cells
        }

        String cellValue = cell.getStringCellValue().trim();

        // Check for empty or spaces only strings and handle them separately
        if (cellValue.isEmpty() || cellValue.isBlank()) {
            return "00:00"; // Default value for empty or spaces only strings
        }

        try {
            // Split the time value into hours and minutes and format accordingly
            String[] parts = cellValue.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            LocalTime parsedTime = LocalTime.of(hours, minutes);

            return parsedTime.toString();
        } catch (Exception e) {
            System.err.println("Error parsing time value: " + cellValue);
            return "00:00"; // Default value for parsing errors
        }
    }


    // Inside analyzeAndPrintResults method
    private static void analyzeAndPrintResults(Map<String, EmployeeData> employeeDataMap) {
        System.out.println("Employee\t7 Consecutive Days\tShort Breaks\tMore Than 14 Hours");
        System.out.println("--------\t-------------------\t-------------\t---------------------");

        for (EmployeeData employeeData : employeeDataMap.values()) {
            System.out.printf("%-15s\t%-19s\t%-13s\t%-21s%n",
                    employeeData.getName(),
                    employeeData.hasWorkedConsecutiveDays(7),
                    employeeData.hasShortBreaksBetweenShifts(Duration.ofHours(1), Duration.ofHours(10)),
                    employeeData.hasWorkedMoreThanDurationInSingleShift(Duration.ofHours(14)));
        }
    }
}

class EmployeeData {
    private String name;
    private Map<LocalDate, WorkDay> workDays = new HashMap<>();

    public void addWorkDay(String position, LocalTime timeIn, LocalTime timeOut,
                           LocalTime timecardHours, LocalDate payCycleStartDate, LocalDate payCycleEndDate) {
        this.name = position; // Assuming position is the name for simplicity
        this.workDays.put(payCycleStartDate, new WorkDay(position, timeIn, timeOut, timecardHours));
    }

    public String getName() {
        return name;
    }

    public boolean hasWorkedConsecutiveDays(int days) {
        // Implement logic to check if the employee has worked for the specified consecutive days
        // You may need to adapt this method based on your specific requirements
        return false;
    }

    public boolean hasShortBreaksBetweenShifts(Duration minBreakDuration, Duration maxBreakDuration) {
        // Implement logic to check if the employee has short breaks between shifts
        // You may need to adapt this method based on your specific requirements
        return false;
    }

    public boolean hasWorkedMoreThanDurationInSingleShift(Duration duration) {
        // Implement logic to check if the employee has worked more than the specified duration in a single shift
        // You may need to adapt this method based on your specific requirements
        return false;
    }
}

class WorkDay {
    private String position;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private LocalTime timecardHours;

    public WorkDay(String position, LocalTime timeIn, LocalTime timeOut, LocalTime timecardHours) {
        this.position = position;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.timecardHours = timecardHours;
    }
}
