/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import java.io.File;

import org.hibernate.cfg.Configuration;

public class SqliteConfig extends Configuration {

	public SqliteConfig(File databaseFile) {
		super();

		// SQLite properties
		super.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
		super.setProperty("hibernate.connection.url", "jdbc:sqlite:" + databaseFile.toString());
		super.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");

		// Auto-update schema
		super.setProperty("hibernate.hbm2ddl.auto", "update");
		super.setProperty("hibernate.show_sql", "false");

	}

}
