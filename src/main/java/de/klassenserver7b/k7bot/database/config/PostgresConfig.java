/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import de.klassenserver7b.k7bot.database.DatabaseType;

public record PostgresConfig(String host, int port, String database, String user, String password)
		implements DatabaseConfig {

	@Override
	public DatabaseType type() {
		return DatabaseType.POSTGRES;
	}

	@Override
	public String toJdbcUrl() {
		return "jdbc:postgresql://" + host + ":" + port + "/" + database;
	}
}
