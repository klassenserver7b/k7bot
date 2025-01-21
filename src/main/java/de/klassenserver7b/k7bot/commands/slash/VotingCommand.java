/**
 * 
 */
package de.klassenserver7b.k7bot.commands.slash;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.listener.VoteReactionListener;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author K7
 *
 */
public class VotingCommand implements TopLevelSlashCommand {

	private static final String[] numbers = new String[] { "", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣",
			"9️⃣", "🔟" };

	/**
	 * 
	 */
	public VotingCommand() {
	}

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {

		if (!(event.getUser().getIdLong() == 675828196389683223L || event.getUser().getIdLong() == 672514862101954570L
				|| event.getUser().getIdLong() == K7Bot.getInstance().getOwnerId())) {
			event.replyEmbeds(EmbedUtils.getErrorEmbed("You are not allowed to do this").build()).setEphemeral(true)
					.queue();
			return;
		}
		LinkedHashMap<String, User> names = new LinkedHashMap<>();

		for (int i = 0; i < 6; i++) {
			OptionMapping mapping = event.getOption("user" + i);
			if (mapping != null) {
				names.put(mapping.getAsMember().getEffectiveName(), mapping.getAsUser());
			}

		}

		StringBuilder strbuilder = new StringBuilder();
		for (int i = 1; i <= names.size(); i++) {
			strbuilder.append(i);
			strbuilder.append(" - ");
			strbuilder.append(names.keySet().stream().toList().get(i - 1));
			strbuilder.append("\n");
		}

		EmbedBuilder builder = EmbedUtils.getBuilderOf(Color.cyan, strbuilder);
		builder.setTitle("Wer ist der dümmste?");
		builder.setFooter("GameMaster: " + event.getUser().getName());

		InteractionHook hook = event.deferReply(false).complete();

		Message mess = hook.sendMessageEmbeds(builder.build()).complete();

		for (int i = 1; i <= names.size(); i++) {
			mess.addReaction(Emoji.fromFormatted(numbers[i])).queue();
		}

		event.getUser().openPrivateChannel().complete().sendMessageEmbeds(builder.build()).queue();

		K7Bot.getInstance().getShardManager().addEventListener(new VoteReactionListener(mess.getIdLong(),
				event.getUser(), names, new GenericMessageSendHandler(hook)));

	}

	@NotNull
    @Override
	public SlashCommandData getCommandData() {

		ArrayList<OptionData> options = new ArrayList<>();

		for (int i = 0; i < Integer
				.parseInt(K7Bot.getInstance().getPropertiesManager().getProperty("votinglimit")); i++) {
			options.add(new OptionData(OptionType.USER, "user" + i, "an user"));
		}

		return Commands.slash("voting", "starts a voting").addOptions(options);
	}

}
