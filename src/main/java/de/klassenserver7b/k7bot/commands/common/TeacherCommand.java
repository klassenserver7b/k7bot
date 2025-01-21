package de.klassenserver7b.k7bot.commands.common;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.TeacherDB;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class TeacherCommand implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String getHelp() {
        return "Zeigt kompletten Namen (inkl. Anrede) zum gewählten Lehrer an. \n - z.B. [prefix]teacher [Lehrerkürzel]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "teacher" };
	}

	@Override
	public HelpCategories getCategory() {
		return HelpCategories.UNKNOWN;
	}

	@Override
	public void performCommand(Member caller, GuildMessageChannel channel, Message message) {

		String[] args = message.getContentStripped().split(" ");

		if (args.length > 1) {

			TeacherDB.Teacher teacher = K7Bot.getInstance().getTeacherDB().getTeacher(args[1]);
			if (teacher == null) {
				channel.sendMessageEmbeds(EmbedUtils.getErrorEmbed("Lehrer mit Kürzel " + args[1] + " konnte nicht gefunden werden!",
										channel.getGuild().getIdLong())
								.setFooter("requested by @" + caller.getEffectiveName())
								.build()).queue();
				return;
			}

            String description = "**Kürzel**: " + args[1] + "\n" +
                    "**Name: **" + teacher.getDecoratedName();

			channel.sendMessageEmbeds(EmbedUtils.getBuilderOf(description, channel.getGuild().getIdLong())
					.setFooter("requested by @" + caller.getEffectiveName()).build()).queue();

		} else {

			SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "teacher [Lehrerkürzel]", caller);

		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void disableCommand() {
		isEnabled = false;
	}

	@Override
	public void enableCommand() {
		isEnabled = true;
	}

}
