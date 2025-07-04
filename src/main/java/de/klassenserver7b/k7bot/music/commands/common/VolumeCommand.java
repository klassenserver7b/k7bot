package de.klassenserver7b.k7bot.music.commands.common;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.HelpCategories;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.concurrent.TimeUnit;

public class VolumeCommand implements ServerCommand {

    private boolean isEnabled;

    @Override
    public String getHelp() {
        return "Legt das Volume für den Bot auf diesem Server fest.\n - z.B. [prefix]volume [Zahl von 1 bis 100]";
    }

    @Override
    public String[] getCommandStrings() {
        return new String[]{"volume"};
    }

    @Override
    public HelpCategories getCategory() {
        return HelpCategories.MUSIC;
    }

    @Override
    public void performCommand(Member m, GuildMessageChannel channel, Message message) {

        if (MusicUtil.failsConditions(new GenericMessageSendHandler(channel), m)) {
            return;
        }

        String[] args = message.getContentDisplay().split(" ");

        MusicUtil.updateChannel(channel);
        if (args.length <= 1) {

            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "volume [int]", m);

        }

        int volume;

        try {
            volume = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "volume [int]", m);
            return;
        }

        if (volume <= 0) {

            channel.sendMessage(
                            "Brudi was willst den du? Wenn du nix hören willst dann lefte doch den channel oder gebe `"
                                    + K7Bot.getInstance().getPrefixMgr().getPrefix(channel.getGuild().getIdLong())
                                    + "stop ` ein :rage:")
                    .complete().delete().queueAfter(10L, TimeUnit.SECONDS);
            return;
        }

        if (volume > 100) {

            channel.sendMessage(
                            "Brudi was willst denn du? Wenn du taub werden willst oder übersteuerte Musik hören willst dann mach es aber lass mich in Ruhe :rage:")
                    .complete().delete().queueAfter(10L, TimeUnit.SECONDS);
            return;
        }

        Guild guild = channel.getGuild();

        MusicController controller = K7Bot.getInstance().getPlayerUtil().getController(guild.getIdLong());
        AudioPlayer player = controller.getPlayer();
        player.setVolume(volume);

        LiteSQL.onUpdate("UPDATE musicutil SET volume = ? WHERE guildId = ?;", volume, channel.getGuild().getIdLong());

        EmbedBuilder builder = EmbedUtils.getDefault(channel.getGuild().getIdLong());
        builder.setFooter("Requested by @" + m.getEffectiveName());
        builder.setTitle("Volume was set to " + volume);
        channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(10L, TimeUnit.SECONDS);

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
