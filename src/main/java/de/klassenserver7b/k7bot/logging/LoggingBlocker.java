/* (C)2026 */
package de.klassenserver7b.k7bot.logging;

import java.util.ArrayList;
import java.util.List;

public class LoggingBlocker {

	private final List<Long> blockedIds;

	public LoggingBlocker() {
		this.blockedIds = new ArrayList<>();
	}

	public boolean isBlocked(long id) {
		return blockedIds.contains(id);
	}

	public void block(long id) {
		blockedIds.add(id);
	}

	public void unblock(long id) {
		blockedIds.remove(id);
	}

}
