package cl.drakescraft.arcana;

import java.util.UUID;

record ArcanaProfile(UUID playerId, Affinity affinity, ArcaneOrigin origin, long experience, long sigils) {
    ArcanaRank rank() { return ArcanaRank.forExperience(experience); }
}
