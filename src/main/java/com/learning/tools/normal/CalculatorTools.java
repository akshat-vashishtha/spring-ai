package com.learning.tools.normal;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Normal / Local Tool for mathematical calculations and unit conversions.
 */
@Slf4j
@Component
public class CalculatorTools {

    @Tool(description = "Perform standard arithmetic operations: ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, PERCENTAGE.")
    public String calculate(
            @ToolParam(description = "Operation name: 'ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE', 'POWER', 'PERCENTAGE'") String operation,
            @ToolParam(description = "First operand or base value") double a,
            @ToolParam(description = "Second operand (percentage value if PERCENTAGE, exponent if POWER, divisor if DIVIDE)") double b) {
        log.info("CalculatorTools.calculate invoked: operation={}, a={}, b={}", operation, a, b);

        if (operation == null) {
            return "Error: Operation cannot be null. Supported operations: ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, PERCENTAGE.";
        }

        double result;
        switch (operation.trim().toUpperCase()) {
            case "ADD", "+" -> result = a + b;
            case "SUBTRACT", "-" -> result = a - b;
            case "MULTIPLY", "*" -> result = a * b;
            case "DIVIDE", "/" -> {
                if (b == 0) {
                    return "Error: Cannot divide by zero.";
                }
                result = a / b;
            }
            case "POWER", "^" -> result = Math.pow(a, b);
            case "PERCENTAGE", "%" -> result = (a * b) / 100.0;
            default -> {
                return String.format("Unsupported operation '%s'. Supported: ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, PERCENTAGE.", operation);
            }
        }
        log.info("CalculatorTools result: {}", result);
        return String.valueOf(result);
    }

    @Tool(description = "Convert values between measurement units: CELSIUS_TO_FAHRENHEIT, FAHRENHEIT_TO_CELSIUS, KM_TO_MILES, MILES_TO_KM, KG_TO_LBS, LBS_TO_KG.")
    public String convertUnit(
            @ToolParam(description = "Value to convert") double value,
            @ToolParam(description = "Conversion type: CELSIUS_TO_FAHRENHEIT, FAHRENHEIT_TO_CELSIUS, KM_TO_MILES, MILES_TO_KM, KG_TO_LBS, LBS_TO_KG") String conversionType) {
        log.info("CalculatorTools.convertUnit invoked: value={}, conversionType={}", value, conversionType);

        if (conversionType == null) {
            return "Error: Conversion type cannot be null.";
        }

        double converted;
        String unitName;
        switch (conversionType.trim().toUpperCase()) {
            case "CELSIUS_TO_FAHRENHEIT" -> {
                converted = (value * 9.0 / 5.0) + 32.0;
                unitName = "°F";
            }
            case "FAHRENHEIT_TO_CELSIUS" -> {
                converted = (value - 32.0) * 5.0 / 9.0;
                unitName = "°C";
            }
            case "KM_TO_MILES" -> {
                converted = value * 0.621371;
                unitName = "miles";
            }
            case "MILES_TO_KM" -> {
                converted = value / 0.621371;
                unitName = "km";
            }
            case "KG_TO_LBS" -> {
                converted = value * 2.20462;
                unitName = "lbs";
            }
            case "LBS_TO_KG" -> {
                converted = value / 2.20462;
                unitName = "kg";
            }
            default -> {
                return "Unsupported conversion type: " + conversionType;
            }
        }
        return String.format("%.2f %s", converted, unitName);
    }
}
