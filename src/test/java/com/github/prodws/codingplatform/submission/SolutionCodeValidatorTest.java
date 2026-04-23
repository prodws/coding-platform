package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolutionCodeValidatorTest {

    private SolutionCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SolutionCodeValidator();
        ReflectionTestUtils.setField(validator, "maxCodeSizeKb", 1);
        ReflectionTestUtils.setField(validator, "maxLineLength", 100);
    }

    @Test
    void shouldPassForValidCode() {
        String validCode = "public class Solution {\n    // some valid code\n}";
        assertDoesNotThrow(() -> validator.validate(validCode));
    }

    @Test
    void shouldThrowExceptionForEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(""));
        assertThrows(IllegalArgumentException.class, () -> validator.validate("   "));
    }

    @Test
    void shouldThrowExceptionForCodeExceedingSizeLimit() {
        String largeCode = "a".repeat(1024 * 2); // 2 KB
        assertThrows(IllegalArgumentException.class, () -> validator.validate(largeCode));
    }

    @Test
    void shouldThrowExceptionForLineExceedingLengthLimit() {
        String longLine = "a".repeat(101);
        String code = "public class Solution {\n" + longLine + "\n}";
        assertThrows(IllegalArgumentException.class, () -> validator.validate(code));
    }

    @Test
    void shouldThrowExceptionForCodeWithSuspiciousPatterns() {
        String suspiciousCode1 = "Runtime.getRuntime().exec(\"rm -rf /\")";
        assertThrows(IllegalArgumentException.class, () -> validator.validate(suspiciousCode1));

        String suspiciousCode2 = "new ProcessBuilder(\"cmd\", \"/c\", \"echo pwned\")";
        assertThrows(IllegalArgumentException.class, () -> validator.validate(suspiciousCode2));

        String suspiciousCode3 = "System.exit(-1)";
        assertThrows(IllegalArgumentException.class, () -> validator.validate(suspiciousCode3));
    }
}
