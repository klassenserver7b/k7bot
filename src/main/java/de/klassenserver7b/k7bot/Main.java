package de.klassenserver7b.k7bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {

            if (args.length == 0) {
                K7Bot.getInstance(false);
            } else {
                K7Bot.getInstance(args[0].equals("--devmode") || args[0].equals("-d"));

            }

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        }
    }

}
