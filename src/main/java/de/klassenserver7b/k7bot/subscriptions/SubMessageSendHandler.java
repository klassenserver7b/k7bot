package de.klassenserver7b.k7bot.subscriptions;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.subscriptions.types.Subscription;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionDeliveryType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;

public class SubMessageSendHandler {

    final Logger log;

    public SubMessageSendHandler(Logger logger) {
        this.log = logger;
    }

    public void provideSubscriptionMessage(Subscription s, MessageCreateData d) {

        SubscriptionDeliveryType type = s.getDeliveryType();
        if (K7Bot.getInstance().isDevMode()) {
            type = SubscriptionDeliveryType.CANARY;
        }

        switch (type) {

            case TEXT_CHANNEL, NEWS: {
                sendGuildMessageChannelMessage(s, d);
                break;
            }
            case PRIVATE_CHANNEL: {
                sendPrivateMessage(s, d);
                break;
            }
            case CANARY: {
                sendCanaryMessage(s, d);
                break;
            }

            case UNKNOWN: {
                throw new IllegalArgumentException(
                        "Unexpected value: UNKNOWN - id: " + SubscriptionDeliveryType.UNKNOWN.getId());
            }

        }

    }

    private void sendGuildMessageChannelMessage(Subscription s, MessageCreateData data) {
        Long chanid = s.getTargetDiscordId();

        GuildChannel gchan = K7Bot.getInstance().getShardManager().getGuildChannelById(chanid);
        GuildMessageChannel channel;

        if (gchan instanceof GuildMessageChannel) {
            channel = (GuildMessageChannel) gchan;
            channel.sendMessage(data).queue();

        } else {
            log.error("Could not find the Discord target for the Subscription\nSubscriptionId: {}\nDiscord(GuildMessageChannel)Id: {}", s.getId(), s.getTargetDiscordId());
        }

    }

    private void sendPrivateMessage(Subscription s, MessageCreateData data) {

        Long chanid = s.getTargetDiscordId();

        PrivateChannel ch = K7Bot.getInstance().getShardManager().getPrivateChannelById(chanid);

        if (ch != null) {

            ch.sendMessage(data).queue();

        } else {
            log.error("Could not find the Discord target for the Subscription\nSubscriptionId: {}\nDiscord(PrivateChannel)Id: {}", s.getId(), s.getTargetDiscordId());
        }

    }

    private void sendCanaryMessage(Subscription s, MessageCreateData data) {

        if (!K7Bot.getInstance().isDevMode() || (s.getDeliveryType() != SubscriptionDeliveryType.CANARY)) {
            return;
        }

        Long chanid = s.getTargetDiscordId();

        GuildChannel gchan = K7Bot.getInstance().getShardManager().getGuildChannelById(chanid);
        GuildMessageChannel channel;

        if (gchan instanceof GuildMessageChannel) {
            channel = (GuildMessageChannel) gchan;

            channel.sendMessage(data).queue();

        } else {
            log.error("Could not find the Discord target for the Subscription\nSubscriptionId: {}\nDiscord(Canary GuildMessageChannel)Id: {}", s.getId(), s.getTargetDiscordId());
        }

    }
}
