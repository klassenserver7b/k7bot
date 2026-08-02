/* (C)2026 */
package de.klassenserver7b.k7bot.database.config;

import org.hibernate.cfg.Configuration;

@SuppressWarnings("unused")
public class MariaDbConfig extends Configuration {

	public MariaDbConfig(String host, int port, String database, String user, String password) {
		super();

		// Mariadb properties
		super.setProperty("hibernate.connection.driver_class", "org.mariadb.jdbc.Driver");
		super.setProperty("hibernate.connection.url", "jdbc:mariadb://" + host + ":" + port + "/" + database);
		super.setProperty("hibernate.connection.username", user);
		super.setProperty("hibernate.connection.password", password);
		super.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

		// Auto-update schema
		super.setProperty("hibernate.hbm2ddl.auto", "update");
		super.setProperty("hibernate.show_sql", "false");

	}

}
