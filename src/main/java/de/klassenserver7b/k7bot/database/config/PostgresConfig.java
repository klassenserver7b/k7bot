/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import org.hibernate.cfg.Configuration;

@SuppressWarnings("unused")
public class PostgresConfig extends Configuration {

	public PostgresConfig(String host, int port, String database, String user, String password) {
		super();

		// Postgres properties
		super.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
		super.setProperty("hibernate.connection.url", "jdbc:postgresql://" + host + ":" + port + "/" + database);
		super.setProperty("hibernate.connection.username", user);
		super.setProperty("hibernate.connection.password", password);
		super.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

		// Auto-update schema
		super.setProperty("hibernate.hbm2ddl.auto", "update");
		super.setProperty("hibernate.show_sql", "false");

	}

}
