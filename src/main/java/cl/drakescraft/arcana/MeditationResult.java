package cl.drakescraft.arcana;

/** Result of a meditation attempt; storage is performed by the command after a successful ritual. */
record MeditationResult(boolean success, ArcanaProfile profile, long experienceGranted, long spiritGranted, long sigilsGranted,
                        double resonanceMultiplier) {
    static MeditationResult unavailable(ArcanaProfile profile) { return new MeditationResult(false, profile, 0, 0, 0, 1.0D); }
}
