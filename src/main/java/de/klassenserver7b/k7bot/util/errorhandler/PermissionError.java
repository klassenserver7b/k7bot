/* (C)2026 */
package de.klassenserver7b.k7bot.util.errorhandler;

import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.concurrent.TimeUnit;

public class PermissionError {
	public static void onPermissionError(GenericMessageSendHandler channel, Member m) {
		(channel.sendMessage("You don't have the permission to do this!" + m.getAsMention()).complete()).delete()
				.queueAfter(10L, TimeUnit.SECONDS);
	}

	public static void onPermissionError(MessageChannel channel, Member m) {
		PermissionError.onPermissionError(new GenericMessageSendHandler(channel), m);
	}

	public static void onPermissionError(InteractionHook hook, Member m) {
		PermissionError.onPermissionError(new GenericMessageSendHandler(hook), m);
	}
}
