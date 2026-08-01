/* (C)2026 */
package de.klassenserver7b.k7bot.database.impl;

import de.klassenserver7b.k7bot.database.Database;
import de.klassenserver7b.k7bot.database.config.DatabaseConfig;
import de.klassenserver7b.k7bot.exceptions.NotImplementedException;

import java.sql.ResultSet;
import java.util.function.Consumer;

public class PostgresDatabase implements Database {

	private final String notImplementedText = this.getClass().getCanonicalName() + " is currently not supported";

	@Override
	public void connect(DatabaseConfig config) {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public void disconnect(Consumer<Exception> exceptionHandler) {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public void disconnect() {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public int update(Consumer<Exception> exceptionHandler, String sqlPattern, Object... params) {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public int update(String sqlPattern, Object... params) {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public ResultSet query(Consumer<Exception> exceptionHandler, String sqlPattern, Object... params) {
		throw new NotImplementedException(notImplementedText);
	}

	@Override
	public ResultSet query(String sqlPattern, Object... params) {
		throw new NotImplementedException(notImplementedText);
	}
}
