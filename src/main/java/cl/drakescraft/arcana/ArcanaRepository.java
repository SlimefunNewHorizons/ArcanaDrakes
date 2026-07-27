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

/** Small SQLite store; player affinity survives restarts without persisting player names. */
final class ArcanaRepository implements AutoCloseable {
    private final Connection connection;

    ArcanaRepository(File dataFolder) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "arcana.db").getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS arcana_profiles (uuid TEXT PRIMARY KEY, affinity TEXT NOT NULL, updated_at TEXT NOT NULL)");
        }
    }

    Optional<ArcanaProfile> find(UUID playerId) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT affinity FROM arcana_profiles WHERE uuid = ?")) {
            query.setString(1, playerId.toString());
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? Optional.of(new ArcanaProfile(playerId, Affinity.valueOf(result.getString(1)))) : Optional.empty();
            }
        }
    }

    ArcanaProfile findOrAssign(UUID playerId) throws SQLException {
        Optional<ArcanaProfile> current = find(playerId);
        if (current.isPresent()) return current.get();
        ArcanaProfile profile = new ArcanaProfile(playerId, Affinity.random());
        save(profile);
        return profile;
    }

    void save(ArcanaProfile profile) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement("INSERT INTO arcana_profiles(uuid, affinity, updated_at) VALUES(?, ?, datetime('now')) ON CONFLICT(uuid) DO UPDATE SET affinity=excluded.affinity, updated_at=excluded.updated_at")) {
            update.setString(1, profile.playerId().toString());
            update.setString(2, profile.affinity().name());
            update.executeUpdate();
        }
    }

    @Override public void close() throws SQLException { connection.close(); }
}
