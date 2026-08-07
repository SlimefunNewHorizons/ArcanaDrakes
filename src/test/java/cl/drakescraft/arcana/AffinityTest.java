package cl.drakescraft.arcana;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AffinityTest {
    @Test
    void randomSelectionOnlyUsesPrimarySchools() {
        for (int attempt = 0; attempt < 100; attempt++) {
            assertTrue(EnumSet.allOf(Affinity.class).contains(Affinity.random()));
        }
    }

    @Test
    void everyPrimaryAffinityHasACompatibleOrigin() {
        for (Affinity affinity : Affinity.values()) {
            assertTrue(ArcaneOrigin.randomFor(affinity).affinity() == affinity);
        }
    }

    /**
     * Chaos is the rare school. If it ever leaked into the plain roll it would land on roughly one
     * player in seven, which would make the strongest affinity the most common one on the server.
     */
    @Test
    void chaosNeverComesOutOfThePlainRoll() {
        for (int attempt = 0; attempt < 2000; attempt++) {
            assertFalse(Affinity.random().rare(), "random() handed out a rare school");
        }
        assertFalse(Affinity.primaries().contains(Affinity.CHAOS));
        assertEquals(6, Affinity.primaries().size());
    }

    @Test
    void rollHonoursTheChanceAtBothEnds() {
        for (int attempt = 0; attempt < 500; attempt++) {
            assertEquals(Affinity.CHAOS, Affinity.roll(1.0D));
            assertFalse(Affinity.roll(0.0D).rare());
        }
    }

    /** A misconfigured chance must not throw or turn into a certainty by accident. */
    @Test
    void rollClampsChancesOutsideZeroToOne() {
        for (int attempt = 0; attempt < 200; attempt++) {
            assertFalse(Affinity.roll(-5.0D).rare());
            assertEquals(Affinity.CHAOS, Affinity.roll(42.0D));
        }
    }

    @Test
    void chaosIsTheOnlyRareSchool() {
        for (Affinity affinity : Affinity.values()) {
            assertEquals(affinity == Affinity.CHAOS, affinity.rare());
        }
    }
}
