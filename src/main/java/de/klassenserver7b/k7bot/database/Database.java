/* (C)2026 */
package de.klassenserver7b.k7bot.database;

import de.klassenserver7b.k7bot.database.config.DatabaseConfig;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public interface Database {
	void connect(DatabaseConfig config) throws IOException, SQLException;

	void disconnect(Consumer<Exception> exceptionHandler);

	void disconnect();

	int update(Consumer<Exception> exceptionHandler, String sqlPattern, Object... params);

	int update(String sqlPattern, Object... params);

	ResultSet query(Consumer<Exception> exceptionHandler, String sqlPattern, Object... params);

	ResultSet query(String sqlPattern, Object... params);
}
