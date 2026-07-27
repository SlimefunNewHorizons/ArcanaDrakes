package cl.drakescraft.arcana;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AffinityTest {
    @Test
    void randomSelectionOnlyUsesPrimarySchools() {
        for (int attempt = 0; attempt < 100; attempt++) {
            assertTrue(EnumSet.allOf(Affinity.class).contains(Affinity.random()));
        }
    }
}
