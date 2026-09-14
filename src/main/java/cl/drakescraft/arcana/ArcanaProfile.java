package cl.drakescraft.arcana;

import java.util.UUID;

public record ArcanaProfile(
        UUID playerId,
        Affinity affinity,
        ArcaneOrigin origin,
        long experience,
        long sigils,
        long spirit,
        TranscendenceTechnique equippedTechnique
) {
    public ArcanaProfile(UUID playerId, Affinity affinity, ArcaneOrigin origin, long experience, long sigils, long spirit) {
        this(playerId, affinity, origin, experience, sigils, spirit, TranscendenceTechnique.NONE);
    }

    public ArcanaRank rank() {
        return ArcanaRank.forExperience(experience);
    }
}
