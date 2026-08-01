/* (C)2026 */
package de.klassenserver7b.k7bot.database;

import de.klassenserver7b.k7bot.K7Bot;

public class SQLManager {

	// @formatter:off
    public static void onCreate(){

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS reactroles(guildId INTEGER NOT NULL, channelId"
                            + " INTEGER, messageId INTEGER NOT NULL, emote VARCHAR NOT NULL, roleId"
                            + " INTEGER NOT NULL, PRIMARY KEY(guildId, channelId, messageId,"
                            + " emote));");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS modlogs(id INTEGER NOT NULL PRIMARY KEY"
                                + " AUTOINCREMENT, guildId INTEGER, memberId INTEGER NOT NULL,"
                                + " requesterId INTEGER NOT NULL, memberName STRING, requesterName"
                                + " STRING, action STRING, reason STRING, date STRING);");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS createdprivatevcs(guildId INTEGER NOT NULL,"
                                + " channelId INTEGER NOT NULL, PRIMARY KEY(guildId, channelId));");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS musicutil(guildId INTEGER NOT NULL PRIMARY KEY,"
                                + " channelId INTEGER, volume INTEGER NOT NULL DEFAULT 10);");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS botutil(guildId INTEGER NOT NULL PRIMARY KEY,"
                                + " syschannelId INTEGER, prefix STRING NOT NULL DEFAULT '-');");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS musiclogs(id INTEGER NOT NULL PRIMARY KEY"
                            + " AUTOINCREMENT, songname STRING, songauthor STRING, guildId INTEGER,"
                            + " timestamp INTEGER);");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS commandlog(id INTEGER NOT NULL PRIMARY KEY"
                                + " AUTOINCREMENT, command STRING, guildId INTEGER, userId INTEGER,"
                                + " timestamp INTEGER);");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS slashcommandlog(id INTEGER NOT NULL PRIMARY KEY"
                                + " AUTOINCREMENT, command STRING, guildId INTEGER, userId INTEGER,"
                                + " timestamp INTEGER, commandstring STRING);");


        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS subscriptions(id INTEGER NOT NULL PRIMARY KEY"
                                + " AUTOINCREMENT, type INTEGER NOT NULL, target INTEGER NOT NULL,"
                                + " targetDcId INTEGER NOT NULL, subscriptionId INTEGER);");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS userreacts(userId INTEGER NOT NULL, guildId"
                                + " INTEGER, messageId INTEGER NOT NULL, emote VARCHAR NOT NULL,"
                                + " PRIMARY KEY(userId, guildId, messageId, emote));");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS messagelogs(messageId INTEGER NOT NULL PRIMARY"
                                + " KEY, guildId INTEGER, timestamp INTEGER, authorId INTEGER,"
                                + " messageText STRING)");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS memechannels(channelId INTEGER NOT NULL PRIMARY"
                                + " KEY)");

        K7Bot.getInstance()
                .getDb()
                .update(
                        "CREATE TABLE IF NOT EXISTS loggingConfig(guildId INTEGER NOT NULL,"
                                + " optionJson TEXT NOT NULL DEFAULT '[]', PRIMARY KEY(guildId))");
    }
}
