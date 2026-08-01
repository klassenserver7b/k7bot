/* (C)2026 */
package de.klassenserver7b.k7bot.database;

import de.klassenserver7b.k7bot.database.config.DatabaseConfig;
import de.klassenserver7b.k7bot.database.impl.MariaDbDatabase;
import de.klassenserver7b.k7bot.database.impl.PostgresDatabase;
import de.klassenserver7b.k7bot.database.impl.SqliteDatabase;

/**
 * ONLY {@link SQLITE} IS CURRENTLY IMPLEMENTED
 */
public enum DatabaseType {
	SQLITE, POSTGRES, MARIADB;

	public static Database createFor(DatabaseConfig config) {
		return switch (config.type()) {
			case DatabaseType.SQLITE -> new SqliteDatabase();
			case DatabaseType.POSTGRES -> new PostgresDatabase();
			case DatabaseType.MARIADB -> new MariaDbDatabase();
		};
	}
}
