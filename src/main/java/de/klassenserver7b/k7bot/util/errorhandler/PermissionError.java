package de.klassenserver7b.k7bot.util.errorhandler;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.TimeUnit;

public class PermissionError {
    public static void onPermissionError(Member m, GuildMessageChannel channel) {
        (channel.sendMessage("You don't have the permission to do this!" + m.getAsMention()).complete()).delete()
                .queueAfter(10L, TimeUnit.SECONDS);
    }
}