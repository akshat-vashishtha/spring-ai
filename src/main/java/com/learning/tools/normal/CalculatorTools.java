package com.learning.tools.normal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Normal / Local Tool for mathematical calculations and unit conversions.
 * Uses Spring AI ToolContext directly to read decimalPrecision.
 */
@Slf4j
@Component
public class CalculatorTools {

    @Tool(description = "Perform standard arithmetic operations: ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, PERCENTAGE.")
    public String calculate(
            @ToolParam(description = "Operation: 'ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE', 'POWER', 'PERCENTAGE'") String operation,
            @ToolParam(description = "First operand") double a,
            @ToolParam(description = "Second operand") double b,
            ToolContext toolContext) {
        double result = switch (operation.trim().toUpperCase()) {
            case "ADD", "+" -> a + b;
            case "SUBTRACT", "-" -> a - b;
            case "MULTIPLY", "*" -> a * b;
            case "DIVIDE", "/" -> (b == 0) ? Double.NaN : a / b;
            case "POWER", "^" -> Math.pow(a, b);
            case "PERCENTAGE", "%" -> (a * b) / 100.0;
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        };

        if (Double.isNaN(result)) {
            return "Error: Cannot divide by zero.";
        }

        if (toolContext != null && toolContext.getContext().containsKey("decimalPrecision")) {
            int precision = Integer.parseInt(toolContext.getContext().get("decimalPrecision").toString());
            return BigDecimal.valueOf(result).setScale(precision, RoundingMode.HALF_UP).toPlainString();
        }
        return String.valueOf(result);
    }

    @Tool(description = "Convert values between units: CELSIUS_TO_FAHRENHEIT, FAHRENHEIT_TO_CELSIUS, KM_TO_MILES, MILES_TO_KM, KG_TO_LBS, LBS_TO_KG.")
    public String convertUnit(
            @ToolParam(description = "Value to convert") double value,
            @ToolParam(description = "Conversion type: CELSIUS_TO_FAHRENHEIT, FAHRENHEIT_TO_CELSIUS, KM_TO_MILES, MILES_TO_KM, KG_TO_LBS, LBS_TO_KG") String conversionType) {
        return switch (conversionType.trim().toUpperCase()) {
            case "CELSIUS_TO_FAHRENHEIT" -> String.format("%.2f °F", (value * 9.0 / 5.0) + 32.0);
            case "FAHRENHEIT_TO_CELSIUS" -> String.format("%.2f °C", (value - 32.0) * 5.0 / 9.0);
            case "KM_TO_MILES" -> String.format("%.2f miles", value * 0.621371);
            case "MILES_TO_KM" -> String.format("%.2f km", value / 0.621371);
            case "KG_TO_LBS" -> String.format("%.2f lbs", value * 2.20462);
            case "LBS_TO_KG" -> String.format("%.2f kg", value / 2.20462);
            default -> "Unsupported conversion type: " + conversionType;
        };
    }
}
