/* (C)2026 */
package de.klassenserver7b.k7bot.database.impl;

import de.klassenserver7b.k7bot.database.Database;
import de.klassenserver7b.k7bot.database.config.DatabaseConfig;
import de.klassenserver7b.k7bot.database.config.SqliteConfig;
import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.function.Consumer;

public class SqliteDatabase implements Database {
	private Connection conn;
	private static final Logger log = LoggerFactory.getLogger("database-Log");

	@Override
	public void connect(DatabaseConfig config) throws IOException, SQLException {
		if (!(config instanceof SqliteConfig sqliteConfig)) {
			throw new IllegalArgumentException("SqliteDatabase requires a SqliteConfig");
		}

		if (sqliteConfig.databaseFile().createNewFile()) {
			log.info("Database file created - assuming first start.");
		}

		conn = DriverManager.getConnection(sqliteConfig.toJdbcUrl());
		log.info("Connection to SQLite has been established.");
	}

	@Override
	public void disconnect(Consumer<Exception> exceptionHandler) {
		try {
			if (conn != null) {
				conn.close();
				log.info("Connection to SQLite has been closed.");
			}
		} catch (SQLException e) {
			exceptionHandler.accept(e);
		}
	}

	@Override
	public void disconnect() {
		this.disconnect((e -> log.error(e.getMessage(), e)));
	}

	/**
	 * Executes an update on the database
	 *
	 * @param sqlPattern The SQL-String with placeholders
	 * @param parameters The parameters to replace the placeholders with
	 * @return The number of rows affected by the update
	 */
	@Override
	public int update(Consumer<Exception> exceptionHandler, String sqlPattern, Object... parameters) {
		// noinspection SqlSourceToSinkFlow
		try (PreparedStatement p = conn.prepareStatement(sqlPattern)) {
			if (parameters.length != p.getParameterMetaData().getParameterCount()) {
				exceptionHandler.accept(new IllegalArgumentException(
						"Invalid SQLString! - parameter count does not match.", new Throwable().fillInStackTrace()));
				return InternalStatusCodes.ERROR.getId();
			}
			for (int i = 0; i < parameters.length; i++) {
				p.setObject(i + 1, parameters[i]);
			}
			return p.executeUpdate();
		} catch (SQLException e) {
			exceptionHandler.accept(e);
			return -1;
		}
	}

	@Override
	public int update(String sqlPattern, Object... params) {
		return this.update((e -> log.error(e.getMessage(), e)), sqlPattern, params);
	}

	@Override
	public ResultSet query(Consumer<Exception> exceptionHandler, String sqlpattern, Object... parameters) {
		try {
			PreparedStatement p = conn.prepareStatement(sqlpattern);
			if (parameters.length != p.getParameterMetaData().getParameterCount()) {
				exceptionHandler.accept(new IllegalArgumentException(
						"Invalid SQLString! - parameter count does not match.", new Throwable().fillInStackTrace()));
			}
			for (int i = 0; i < parameters.length; i++) {
				p.setObject(i + 1, parameters[i]);
			}
			return p.executeQuery();
		} catch (SQLException e) {
			exceptionHandler.accept(e);
			return null;
		}
	}

	@Override
	public ResultSet query(String sqlPattern, Object... params) {
		return this.query((e -> log.error(e.getMessage(), e)), sqlPattern, params);
	}
}
