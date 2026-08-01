/* (C)2026 */
package de.klassenserver7b.k7bot.util.customapis.types;

import de.klassenserver7b.k7bot.util.InternalStatusCodes;

/**
 * @author Klassenserver7b
 */
public interface LoopedEvent {

	/**
	 * Default method -> called in a loop
	 *
	 * @return {@link InternalStatusCodes statuscode} as an int
	 */
	InternalStatusCodes checkforUpdates();

	/**
	 * Used to check if event is available again after error
	 *
	 * @return is the event is available
	 */
	boolean isAvailable();

	/**
	 *
	 */
	void shutdown();

	/**
	 * @return if the restart was susccessful
	 */
	boolean restart();

	/**
	 * @return the events identifier as a String
	 */
	String getIdentifier();
}
