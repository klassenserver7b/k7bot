package de.klassenserver7b.k7bot.subscriptions.commands;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionDeliveryType;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SubscribeSlashCommand implements TopLevelSlashCommand {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void performSlashCommand(SlashCommandInteraction event) {

        InteractionHook hook = event.deferReply(true).complete();

        String path = event.getFullCommandName();

        SubscriptionTarget target = SubscriptionTarget.valueOf(event.getOption("target").getAsString());

        if (target.isprivileged() && event.getUser().getIdLong() != K7Bot.getInstance().getOwnerId()) {
            hook.sendMessageEmbeds(EmbedUtils.getErrorEmbed(
                            "You must be the Botowner to access this target! - please ask him to create the subscription\nIf you are the Botowner check if you have inserted your discord userid in the configfile!")
                    .build()).queue();
            return;
        }

        if (path.equalsIgnoreCase("subscribe privatechannel")) {

            try {

                K7Bot.getInstance().getSubscriptionManager().createSubscription(
                        SubscriptionDeliveryType.PRIVATE_CHANNEL, target,
                        event.getUser().openPrivateChannel().complete().getIdLong());

                hook.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("The subscription was created successfull!").build())
                        .queue();

            } catch (Exception e) {

                log.error(e.getMessage(), e);
                hook.sendMessageEmbeds(EmbedUtils.getErrorEmbed(
                                "Could not open a private channel! please check if you have the option `get DM's from server members` in the `Privacy & Safety` settings enabled!")
                        .build()).queue();

            }

        } else {
            GuildChannelUnion union = event.getOption("channel").getAsChannel();
            SubscriptionDeliveryType delivery;

            if (!K7Bot.getInstance().isDevMode()) {
                switch (union.getType()) {

                    case TEXT -> delivery = SubscriptionDeliveryType.TEXT_CHANNEL;
                    case NEWS -> delivery = SubscriptionDeliveryType.NEWS;
                    default -> {
                        delivery = SubscriptionDeliveryType.UNKNOWN;
                        hook.sendMessageEmbeds(EmbedUtils.getErrorEmbed(
                                        "Can't create subscription in " + union.getType() + "!\nPlease use a Text or News Channel")
                                .build()).queue();
                    }
                }
            } else {

                delivery = SubscriptionDeliveryType.CANARY;

            }

            if (delivery != SubscriptionDeliveryType.UNKNOWN) {

                K7Bot.getInstance().getSubscriptionManager().createSubscription(delivery, target,
                        union.getIdLong());
                hook.sendMessageEmbeds(EmbedUtils.getSuccessEmbed("The subscription was created successfull!").build())
                        .queue();
            }

        }

    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {

        List<Choice> choices = new ArrayList<>();

        for (SubscriptionTarget t : SubscriptionTarget.values()) {

            if (t == SubscriptionTarget.UNKNOWN) {
                continue;
            }

            choices.add(new Choice(t.toString(), t.toString()));

        }

        SubcommandData GuildMessageChannelsub = new SubcommandData("messagechannel",
                "Use this if you want to recieve your messages in a Text-Channel")
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "The channel where the message should be send to")
                                .setRequired(true)
                                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM))
                .addOptions(new OptionData(OptionType.STRING, "target",
                        "The target the subscription should check for updates").addChoices(choices).setRequired(true));

        SubcommandData privatechannelsub = new SubcommandData("privatechannel",
                "Use this if you want to recieve your messages in a private-Channel")
                .addOptions(new OptionData(OptionType.STRING, "target",
                        "The target the subscription should check for updates").addChoices(choices).setRequired(true));

        return Commands.slash("subscribe", "Addes a subscription for the given type in the given channel.")
                .addSubcommands(GuildMessageChannelsub, privatechannelsub)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

}
