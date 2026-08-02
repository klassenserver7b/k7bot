/* (C)2026 */
package de.klassenserver7b.k7bot.commands.generic.moderation;

import java.util.List;

import de.klassenserver7b.k7bot.util.errorhandler.PermissionError;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * @author K7
 */
public abstract class GenericMemberLogsCommand {

	/**
	 * @param m       The {@link Member} who executed the command
	 * @param channel The {@link GuildMessageChannel} where the command was executed
	 * @return true if the {@link Member} doesn't have the required permissions
	 */
	protected boolean MembFailsPermissions(Member m, GuildMessageChannel channel) {
		if (!m.hasPermission(Permission.KICK_MEMBERS)) {
			PermissionError.onPermissionError(channel, m);
			return true;
		}
		return false;
	}

	/**
	 * @param channel The {@link GuildMessageChannel} where the command was executed
	 * @param message The {@link Message} that was sent
	 * @param m       The {@link Member} who executed the command
	 * @return A {@link List} of {@link Member Members} that were mentioned in the
	 *         message
	 * @throws IllegalArgumentException if no {@link Member Members} were mentioned
	 */
	protected List<Member> getMembersFromMessage(GuildMessageChannel channel, Message message, Member m)
			throws IllegalArgumentException {
		List<Member> memb = message.getMentions().getMembers();

		if (memb.isEmpty()) {
			SyntaxError.onCmdSyntaxError(channel, m, "modlogs [@moderator]");
			throw new IllegalArgumentException();
		}

		return memb;
	}
}
