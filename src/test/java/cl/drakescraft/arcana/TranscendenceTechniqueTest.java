package cl.drakescraft.arcana;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TranscendenceTechniqueTest {

    @Test
    void allTechniquesHaveUniquePathsAndRanks() {
        for (TranscendenceTechnique t : TranscendenceTechnique.values()) {
            assertNotNull(t.displayName());
            assertNotNull(t.minRank());
            if (t != TranscendenceTechnique.NONE) {
                assertNotNull(t.path());
                assertFalse(t.description().isEmpty());
                assertTrue(t.cooldownSeconds() > 0);
            }
        }
    }

    @Test
    void pathsGroupExpectedTechniques() {
        assertEquals(3, TranscendenceTechnique.byPath(TranscendencePath.DRAGON_BALL).size());
        assertEquals(3, TranscendenceTechnique.byPath(TranscendencePath.NARUTO).size());
        assertEquals(3, TranscendenceTechnique.byPath(TranscendencePath.ONE_PIECE).size());
    }

    @Test
    void profileConstructorsMaintainBackwardCompatibility() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        ArcanaProfile profileOld = new ArcanaProfile(uuid, Affinity.FIRE, ArcaneOrigin.PHOENIX, 1000L, 5L, 50L);
        assertEquals(TranscendenceTechnique.NONE, profileOld.equippedTechnique());

        ArcanaProfile profileNew = new ArcanaProfile(uuid, Affinity.FIRE, ArcaneOrigin.PHOENIX, 1000L, 5L, 50L, TranscendenceTechnique.GENKIDAMA);
        assertEquals(TranscendenceTechnique.GENKIDAMA, profileNew.equippedTechnique());
    }
}
