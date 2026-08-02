/* (C)2026 */
package de.klassenserver7b.k7bot.util.errorhandler;

import java.util.concurrent.TimeUnit;

import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class SyntaxError {
	public static void onCmdSyntaxError(GenericMessageSendHandler channel, Member m, String syntax) {
		channel.sendMessage("Please use the following syntax: " + "[prefix]" + syntax + m.getAsMention()).complete()
				.delete().queueAfter(10L, TimeUnit.SECONDS);
	}

	public static void onCmdSyntaxError(MessageChannel channel, Member m, String syntax) {
		SyntaxError.onCmdSyntaxError(new GenericMessageSendHandler(channel), m, syntax);
	}

	@SuppressWarnings("unused")
	public static void onCmdSyntaxError(InteractionHook hook, Member m, String syntax) {
		SyntaxError.onCmdSyntaxError(new GenericMessageSendHandler(hook), m, syntax);
	}
}
