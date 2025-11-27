/**
 *
 */
package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.util.RestartUtil;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author K7
 */
public class JDAReconnectListener extends ListenerAdapter {

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {

        RestartUtil.restart();

    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        RestartUtil.restart();

    }

}
