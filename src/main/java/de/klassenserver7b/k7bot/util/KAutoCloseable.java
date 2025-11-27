package de.klassenserver7b.k7bot.util;

public interface KAutoCloseable extends AutoCloseable {
    @Override
    void close();
}
