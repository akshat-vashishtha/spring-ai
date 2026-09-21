package com.learning.tools.normal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Normal / Local Tool for date and time calculations.
 * Demonstrates Pure Context-Only Tool: reads client timezone directly from ToolContext.
 */
@Slf4j
@Component
public class DateTimeTools {

    /**
     * Pure Context-Only Tool: Requires NO LLM parameters.
     * Extracts timezone directly from ToolContext (falls back to UTC if omitted).
     */
    @Tool(description = "Get the current date and time in the user's configured timezone.")
    public String getCurrentDateTime(ToolContext toolContext) {
        String timeZone = "UTC";
        if (toolContext != null && toolContext.getContext().containsKey("timeZone")) {
            timeZone = String.valueOf(toolContext.getContext().get("timeZone"));
            log.info("DateTimeTools: using timezone from ToolContext: {}", timeZone);
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone.trim()));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)"));
    }

    /**
     * Standard Tool: Takes date parameters from the LLM.
     */
    @Tool(description = "Calculate days between two dates in ISO format (yyyy-MM-dd).")
    public String calculateDaysBetween(
            @ToolParam(description = "Start date (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "End date (yyyy-MM-dd)") String endDate) {
        LocalDate start = LocalDate.parse(startDate.trim());
        LocalDate end = LocalDate.parse(endDate.trim());
        long days = ChronoUnit.DAYS.between(start, end);
        return String.format("Number of days between %s and %s is %d days.", start, end, days);
    }
}
