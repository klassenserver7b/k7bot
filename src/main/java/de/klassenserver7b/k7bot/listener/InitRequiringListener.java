/**
 *
 */
package de.klassenserver7b.k7bot.listener;

import java.util.concurrent.CompletableFuture;

/**
 * @author K7
 */

@FunctionalInterface
public interface InitRequiringListener {

    /**
     * Initializes the Listener (usually checking stuff happened in off time)
     *
     * @return {@link CompletableFuture} which retuns the "exit code" of the
     * inmitialization
     */
    CompletableFuture<Integer> initialize();

}
