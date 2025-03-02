package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

            try (ResultSet set = LiteSQL.onQuery(
                    "SELECT roleId FROM reactroles WHERE guildId = ? AND channelId = ? AND messageId = ? AND emote = ?;",
                    guildId, channelId, messageId, emote.getName())) {

                if (set.next()) {
                    long roleId = set.getLong("roleId");

                    Guild guild = event.getGuild();
                    Member member = event.getMember();

                    if (member == null) {
                        return;
                    }

                    if (add) {
                        guild.addRoleToMember(member, guild.getRoleById(roleId)).queue();

                        LiteSQL.onUpdate(
                                "INSERT OR REPLACE INTO userreacts(userId, guildId, messageId, emote) VALUES(?,?,?,?);",
                                event.getUserIdLong(), guildId, messageId, emote.getName());
                    } else {
                        guild.removeRoleFromMember(member, guild.getRoleById(roleId)).queue();

                        LiteSQL.onUpdate(
                                "REMOVE FROM userreacts WHERE userId = ? AND guildId = ? AND messageId = ? AND emote = ?;",
                                event.getUserIdLong(), guildId, messageId, emote.getName());
                    }
                }
            } catch (SQLException | IllegalArgumentException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Initializes the Listener (checks for reactions happened in off time)
     *
     * @return {@link CompletableFuture} which retuns the "exit code" of the
     * initialization
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

            try (ResultSet reactRoles = LiteSQL
                    .onQuery("SELECT channelId, messageId, emote, roleId FROM reactroles;")) {
                // loop through all registered reaction roles
                while (reactRoles.next()) {

                    /*
                     * retrieving the GuildChannel which should always be a GuildMessageChannel
                     * (can't create reactions in other than that)
                     */

                    GuildMessageChannel msgChannel = (GuildMessageChannel) K7Bot.getInstance()
                            .getShardManager().getGuildChannelById(reactRoles.getLong("channelId"));

                    // get Objects from db data
                    long messageId = reactRoles.getLong("messageId");

                    Message mess = msgChannel.retrieveMessageById(messageId).complete();

                    Guild guild = mess.getGuild();
                    Role role = guild.getRoleById(reactRoles.getLong("roleId"));

                    String emoji = reactRoles.getString("emote");

                    MessageReaction reaction = mess.getReaction(Emoji.fromFormatted(emoji));

                    List<Long> userIds = new ArrayList<>();
                    if (reaction != null) {
                        for (User u : reaction.retrieveUsers().complete()) {
                            if (!u.isBot()) {
                                userIds.add(u.getIdLong());
                            }
                        }
                    }

                    String sql = "SELECT userId from userreacts WHERE messageId = ? AND emote = ?;";

                    try (ResultSet oldUserReactData = LiteSQL.onQuery(sql, messageId, emoji)) {

                        // loop through all data logged while the bot was running and resolving changes
                        while (oldUserReactData.next()) {
                            long dbUserId = oldUserReactData.getLong("userId");

                            /*
                             * Remove roles, db entry, userIds entry from the users that have removed their
                             * reaction
                             */
                            if (checkRoleRemove(userIds, messageId, emoji, role, guild, dbUserId)) {
                                userIds.remove(dbUserId);
                            }
                        }
                    }

                    // Add roles to every user which wasn't logged but has now reacted
                    addRoles(userIds, messageId, emoji, role, guild);

                }

            } catch (SQLException e) {
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
         * means 'has removed')
         */
        protected boolean checkRoleRemove(List<Long> userIds, long messageId, String emoji, Role role, Guild guild,
                                          long dbUserId) {

            if (!userIds.contains(dbUserId)) {
                guild.removeRoleFromMember(UserSnowflake.fromId(dbUserId), role).queue();
                LiteSQL.onUpdate("DELETE FROM userreacts WHERE userId = ? AND messageId = ? AND emote=?", dbUserId,
                        messageId, emoji);
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
                LiteSQL.onUpdate(
                        "INSERT OR REPLACE INTO userreacts(userId, guildId, messageId, emote) VALUES(?,?,?,?);", userId,
                        guild.getIdLong(), messageId, emoji);

            }
        }

    }

}