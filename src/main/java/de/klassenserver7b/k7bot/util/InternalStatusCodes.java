/* (C)2026 */
package de.klassenserver7b.k7bot.util;

/**
 * @author K7
 *
 */
@SuppressWarnings("unused")
public enum InternalStatusCodes {
	ERROR(-1), FAILURE(1), SUCCESS(0), INVALID_CONFIG(5), SQL_ERROR(10);

	private final int id;

	InternalStatusCodes(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
