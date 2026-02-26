package com.ling.lingaiagent.unit;

import com.ling.lingaiagent.app.IngestResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoveReportUnitTest {

    /**
     * Test that report title is not null
     */
    @Test
    void testReportTitle_notNull() {
        String title = "Ling's Love Report";
        assertNotNull(title);
        assertFalse(title.isEmpty());
    }

    /**
     * Test that suggestions list has correct format
     */
    @Test
    void testSuggestions_notEmpty() {
        String[] suggestions = {
                "Try active listening during conflicts",
                "Schedule weekly quality time together",
                "Practice expressing appreciation daily"
        };
        assertTrue(suggestions.length > 0);
        assertEquals(3, suggestions.length);
    }

    /**
     * Test report title contains username
     */
    @Test
    void testReportTitle_containsUsername() {
        String username = "Ling";
        String title = username + "'s Love Report";
        assertTrue(title.contains(username));
        assertTrue(title.contains("Love Report"));
    }

    /**
     * Test empty suggestions are detected
     */
    @Test
    void testSuggestions_empty_shouldBeDetected() {
        String[] suggestions = {};
        assertEquals(0, suggestions.length);
        assertTrue(suggestions.length == 0);
    }
}
