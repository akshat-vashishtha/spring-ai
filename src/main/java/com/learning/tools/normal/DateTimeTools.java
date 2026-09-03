package com.learning.tools.normal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Normal / Local Tool for date and time calculations.
 */
@Slf4j
@Component
public class DateTimeTools {

    @Tool(description = "Get the current date and time in a specified timezone. Defaults to UTC if no timezone is provided.")
    public String getCurrentDateTime(
            @ToolParam(description = "Zone ID such as 'UTC', 'Asia/Kolkata', 'America/New_York', 'Europe/London'", required = false) String timeZone) {
        log.info("DateTimeTools.getCurrentDateTime invoked with timeZone: {}", timeZone);
        try {
            ZoneId zone = (timeZone == null || timeZone.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(timeZone.trim());
            ZonedDateTime now = ZonedDateTime.now(zone);
            String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)"));
            log.info("DateTimeTools result: {}", formatted);
            return formatted;
        } catch (Exception e) {
            log.warn("Invalid timeZone '{}', falling back to UTC. Error: {}", timeZone, e.getMessage());
            return ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)"));
        }
    }

    @Tool(description = "Calculate the number of days between two dates in ISO format (yyyy-MM-dd).")
    public String calculateDaysBetween(
            @ToolParam(description = "Start date in yyyy-MM-dd format, e.g., '2026-01-01'") String startDate,
            @ToolParam(description = "End date in yyyy-MM-dd format, e.g., '2026-12-31'") String endDate) {
        log.info("DateTimeTools.calculateDaysBetween invoked with startDate: {}, endDate: {}", startDate, endDate);
        try {
            LocalDate start = LocalDate.parse(startDate.trim());
            LocalDate end = LocalDate.parse(endDate.trim());
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("Number of days between %s and %s is %d days.", startDate, endDate, days);
        } catch (Exception e) {
            log.error("Failed to calculate days between dates: startDate={}, endDate={}", startDate, endDate, e);
            return "Error calculating days: " + e.getMessage() + ". Please provide valid dates in yyyy-MM-dd format.";
        }
    }
}
