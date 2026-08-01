/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import de.klassenserver7b.k7bot.database.DatabaseType;

public sealed interface DatabaseConfig permits SqliteConfig, PostgresConfig, MariaDbConfig {

	DatabaseType type();

	// shared JDBC URL builder responsibility lives per-implementation
	String toJdbcUrl();
}
