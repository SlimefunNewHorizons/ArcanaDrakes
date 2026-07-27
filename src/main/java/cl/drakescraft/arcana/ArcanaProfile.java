package cl.drakescraft.arcana;

import java.util.UUID;

record ArcanaProfile(UUID playerId, Affinity affinity, ArcaneOrigin origin, long experience, long sigils, long spirit) {
    ArcanaRank rank() { return ArcanaRank.forExperience(experience); }
}
