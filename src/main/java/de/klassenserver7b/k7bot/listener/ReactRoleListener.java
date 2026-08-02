/* (C)2026 */
package de.klassenserver7b.k7bot.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.database.dao.ReactRolesDAO;
import de.klassenserver7b.k7bot.database.dao.UserReactsDAO;
import de.klassenserver7b.k7bot.database.entities.ReactRolesEntity;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactRoleListener extends ListenerAdapter implements InitRequiringListener {

	private final Logger log;

	public ReactRoleListener() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
		performAction(event, true);
	}

	@Override
	public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
		performAction(event, false);
	}

	protected void performAction(GenericMessageReactionEvent event, boolean add) {
		if (event.getChannelType() == ChannelType.TEXT) {
			long guildId = event.getGuild().getIdLong();
			long channelId = event.getChannel().getIdLong();
			long messageId = event.getMessageIdLong();

			if (event.retrieveUser().complete().isBot()) {
				return;
			}

			EmojiUnion emote = event.getEmoji();

			try {
				ReactRolesEntity reactRole = new ReactRolesDAO().getRole(guildId, channelId, messageId, emote.getName())
						.join();

				if (reactRole != null) {
					long roleId = reactRole.getRoleId();

					Guild guild = event.getGuild();
					Member member = event.getMember();

					if (member == null) {
						return;
					}

					Role r = guild.getRoleById(roleId);
					if (r == null) {
						log.warn("ReactRole Role not found by JDA - deleting");
						new ReactRolesDAO().deleteByRoleId(roleId);
						return;
					}

					if (add) {
						guild.addRoleToMember(member, r).queue();
						new UserReactsDAO().insertReaction(event.getUserIdLong(), guildId, messageId, emote.getName())
								.join();
					} else {
						guild.removeRoleFromMember(member, r).queue();
						new UserReactsDAO().deleteReaction(event.getUserIdLong(), guildId, messageId, emote.getName())
								.join();
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Initializes the Listener (checks for reactions happened in off time)
	 *
	 * @return {@link CompletableFuture} which retuns the "exit code" of the
	 *         initialization
	 */
	@Override
	public CompletableFuture<Integer> initialize() {

		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

		new ReactRoleRunnable(completableFuture).start();

		return completableFuture;
	}

	/**
	 * SubClass representing the {@link Runnable} for the {@link CompletableFuture}
	 * of {@link ReactRoleListener#initialize()}
	 *
	 * @author K7
	 */
	protected class ReactRoleRunnable implements Runnable {

		private final CompletableFuture<Integer> completableFuture;

		public ReactRoleRunnable(CompletableFuture<Integer> future) {
			this.completableFuture = future;
		}

		public void start() {
			Thread.startVirtualThread(this);
		}

		@Override
		public void run() {

			try {
				java.util.List<ReactRolesEntity> reactRoles = new ReactRolesDAO().getAllRoles().join();
				// loop through all registered reaction roles
				for (ReactRolesEntity reactRole : reactRoles) {

					/*
					 * retrieving the GuildChannel which should always be a GuildMessageChannel
					 * (can't create reactions in other than that)
					 */

					GuildChannel guildChannel = K7Bot.getInstance().getShardManager()
							.getGuildChannelById(reactRole.getChannelId());

					if (guildChannel == null) {
						log.warn("ReactRole Channel not found by JDA - deleting");
						new ReactRolesDAO().deleteByChannelId(reactRole.getChannelId());
						return;
					}

					if (guildChannel instanceof GuildMessageChannel msgChannel) {

						// get Objects from db data
						long messageId = reactRole.getMessageId();

						Message mess = msgChannel.retrieveMessageById(messageId).complete();

						Guild guild = mess.getGuild();
						Role role = guild.getRoleById(reactRole.getRoleId());

						String emoji = reactRole.getEmote();

						MessageReaction reaction = mess.getReaction(Emoji.fromFormatted(emoji));

						List<Long> userIds = new ArrayList<>();
						if (reaction != null) {
							for (User u : reaction.retrieveUsers().complete()) {
								if (!u.isBot()) {
									userIds.add(u.getIdLong());
								}
							}
						}

						java.util.List<Long> oldUserReactData = new UserReactsDAO().getUsersByReaction(messageId, emoji)
								.join();

						// loop through all data logged while the bot wasn't running and resolving
						// changes
						for (long dbUserId : oldUserReactData) {
							if (checkRoleRemove(userIds, messageId, emoji, role, guild, dbUserId)) {
								userIds.remove(dbUserId);
							}
						}

						// Add roles to every user which wasn't logged but has now reacted
						addRoles(userIds, messageId, emoji, role, guild);
					}
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				completableFuture.complete(1);
				return;
			}
			completableFuture.complete(0);
		}

		/**
		 * Remove roles from all users that have removed their reaction and remove their
		 * db entry
		 *
		 * @param userIds   {@link List} with all users that have currently reacted
		 * @param messageId {@link Message} of the ReactRole
		 * @param emoji     {@link String Emoji} the users have/had to react with
		 * @param role      {@link Role} that should get removed
		 * @param guild     {@link Guild} the Guild of the Message
		 * @param dbUserId  {@link Long} The {@link UserSnowflake} of the user which
		 *                  should be checked
		 * @return A boolean representing if the user has removed their reaction (true
		 *         means 'has removed')
		 */
		protected boolean checkRoleRemove(List<Long> userIds, long messageId, String emoji, Role role, Guild guild,
				long dbUserId) {

			if (!userIds.contains(dbUserId)) {
				guild.removeRoleFromMember(UserSnowflake.fromId(dbUserId), role).queue();
				new UserReactsDAO().deleteReaction(dbUserId, guild.getIdLong(), messageId, emoji).join();
				return true;
			}

			return false;
		}

		/**
		 * Add roles from all users that have added a reaction and add a db entry for
		 * them
		 *
		 * @param userIds   {@link List} with all users that have currently reacted
		 * @param messageId {@link Message} of the ReactRole
		 * @param emoji     {@link String Emoji} the users have/had to react with
		 * @param role      {@link Role} that should be granted
		 * @param guild     {@link Guild} the Guild of the Message
		 */
		protected void addRoles(List<Long> userIds, long messageId, String emoji, Role role, Guild guild) {
			for (long userId : userIds) {

				guild.addRoleToMember(UserSnowflake.fromId(userId), role).queue();
				new UserReactsDAO().insertReaction(userId, guild.getIdLong(), messageId, emoji).join();
			}
		}
	}
}
