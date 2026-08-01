/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import de.klassenserver7b.k7bot.database.DatabaseType;
import java.io.File;

public record SqliteConfig(File databaseFile) implements DatabaseConfig {

	@Override
	public DatabaseType type() {
		return DatabaseType.SQLITE;
	}

	@Override
	public String toJdbcUrl() {
		return "jdbc:sqlite:" + databaseFile.getPath();
	}
}
