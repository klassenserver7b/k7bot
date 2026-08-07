/* (C)2026 */
package de.klassenserver7b.k7bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	static void main() {
		try {
			K7Bot.getInstance();
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}
	}

}
