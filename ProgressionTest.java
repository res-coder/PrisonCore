package com.seandoyle.prisoncore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressionTest {
    @Test void xpGrows() {
        assertEquals(100, Progression.xpRequired(1, 100, 1.35));
        assertTrue(Progression.xpRequired(20, 100, 1.35) > Progression.xpRequired(10, 100, 1.35));
    }
    @Test void upgradeCostGrows() {
        assertEquals(150, Progression.upgradeCost(150, 1.65, 0));
        assertTrue(Progression.upgradeCost(150, 1.65, 3) > 150);
    }
    @Test void rejectsBadInputs() {
        assertThrows(IllegalArgumentException.class, () -> Progression.xpRequired(0, 100, 1.2));
    }
}
