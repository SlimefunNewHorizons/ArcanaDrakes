package cl.drakescraft.arcana;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

/** Small SQLite store; player affinity and techniques survive restarts without persisting player names. */
final class ArcanaRepository implements AutoCloseable {
    private final Connection connection;
    /** Chance of Chaos on a first join. Settable so a config reload takes effect without a restart. */
    private double chaosChance;

    ArcanaRepository(File dataFolder) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "arcana.db").getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS arcana_profiles (uuid TEXT PRIMARY KEY, affinity TEXT NOT NULL, origin TEXT NOT NULL DEFAULT 'PHOENIX', experience INTEGER NOT NULL DEFAULT 0, sigils INTEGER NOT NULL DEFAULT 0, spirit INTEGER NOT NULL DEFAULT 0, technique TEXT NOT NULL DEFAULT 'NONE', updated_at TEXT NOT NULL)");
            ensureColumn(statement, "origin", "TEXT NOT NULL DEFAULT 'PHOENIX'");
            ensureColumn(statement, "experience", "INTEGER NOT NULL DEFAULT 0");
            ensureColumn(statement, "sigils", "INTEGER NOT NULL DEFAULT 0");
            ensureColumn(statement, "spirit", "INTEGER NOT NULL DEFAULT 0");
            ensureColumn(statement, "technique", "TEXT NOT NULL DEFAULT 'NONE'");
        }
    }

    void chaosChance(double chance) { this.chaosChance = chance; }

    Optional<ArcanaProfile> find(UUID playerId) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT affinity, origin, experience, sigils, spirit, technique FROM arcana_profiles WHERE uuid = ?")) {
            query.setString(1, playerId.toString());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) return Optional.empty();
                Affinity affinity = Affinity.valueOf(result.getString(1));
                ArcaneOrigin origin = ArcaneOrigin.valueOf(result.getString(2));
                long xp = result.getLong(3);
                long sigils = result.getLong(4);
                long spirit = result.getLong(5);
                TranscendenceTechnique tech = TranscendenceTechnique.NONE;
                try {
                    String techStr = result.getString(6);
                    if (techStr != null) tech = TranscendenceTechnique.valueOf(techStr);
                } catch (Exception ignored) {}

                return Optional.of(new ArcanaProfile(playerId, affinity, origin, xp, sigils, spirit, tech));
            }
        }
    }

    ArcanaProfile findOrAssign(UUID playerId) throws SQLException {
        Optional<ArcanaProfile> current = find(playerId);
        if (current.isPresent()) return current.get();
        Affinity affinity = Affinity.roll(chaosChance);
        ArcanaProfile profile = new ArcanaProfile(playerId, affinity, ArcaneOrigin.randomFor(affinity), 0L, 0L, 0L, TranscendenceTechnique.NONE);
        save(profile);
        return profile;
    }

    void save(ArcanaProfile profile) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement("INSERT INTO arcana_profiles(uuid, affinity, origin, experience, sigils, spirit, technique, updated_at) VALUES(?, ?, ?, ?, ?, ?, ?, datetime('now')) ON CONFLICT(uuid) DO UPDATE SET affinity=excluded.affinity, origin=excluded.origin, experience=excluded.experience, sigils=excluded.sigils, spirit=excluded.spirit, technique=excluded.technique, updated_at=excluded.updated_at")) {
            update.setString(1, profile.playerId().toString());
            update.setString(2, profile.affinity().name());
            update.setString(3, profile.origin().name());
            update.setLong(4, profile.experience());
            update.setLong(5, profile.sigils());
            update.setLong(6, profile.spirit());
            update.setString(7, profile.equippedTechnique() != null ? profile.equippedTechnique().name() : TranscendenceTechnique.NONE.name());
            update.executeUpdate();
        }
    }

    @Override public void close() throws SQLException { connection.close(); }

    private static void ensureColumn(Statement statement, String column, String definition) throws SQLException {
        try { statement.executeUpdate("ALTER TABLE arcana_profiles ADD COLUMN " + column + " " + definition); }
        catch (SQLException ignored) { /* Column already exists on an upgraded world. */ }
    }
}
