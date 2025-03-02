package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.K7Bot;
import de.klassenserver7b.k7bot.logging.LoggingFilter;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.util.KAutoCloseable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VoiceListener extends ListenerAdapter {
    public final List<Long> tempchannels = new ArrayList<>();

    private final Logger log;

    public VoiceListener() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {

        AudioChannelUnion oldchan = event.getChannelLeft();
        AudioChannelUnion newchan = event.getChannelJoined();

        switch ((oldchan == null ? 0 : 1) | (newchan == null ? 0 : 2)) {
            case 1 -> onLeave(oldchan);
            case 2 -> onJoin(newchan, event.getMember());
            case 3 -> onMove(oldchan, newchan, event.getMember());
        }

    }

    protected void onMove(AudioChannel oldChan, AudioChannel newChan, Member member) {
        onLeave(oldChan);
        onJoin(newChan, member);
    }

    protected void onJoin(AudioChannel audioChannel, Member member) {
        if (audioChannel.getIdLong() == 841212695259775027L) {
            VoiceChannel voice = (VoiceChannel) audioChannel;
            Category cat = voice.getParentCategory();

            VoiceChannel vc;

            try (KAutoCloseable ignored = LoggingFilter.getInstance().blockEventExecution()) {

                if (cat != null) {
                    vc = cat.createVoiceChannel(member.getEffectiveName() + "s Voicechannel").complete();
                } else {
                    vc = voice.getGuild().createVoiceChannel(member.getEffectiveName() + "s Voicechannel").complete();
                }
                LoggingFilter.getInstance().getLoggingBlocker().block(vc.getIdLong());
            }

            vc.getManager().setUserLimit(voice.getUserLimit()).queue();
            Guild controller = vc.getGuild();
            controller.moveVoiceMember(member, vc).queue();

            LiteSQL.onUpdate("INSERT INTO createdprivatevcs(guildId, channelId) VALUES(?, ?);",
                    vc.getGuild().getIdLong(), vc.getIdLong());

            K7Bot.getInstance().getMainLogger().info("Created custom VoiceChannel for Member: {} with the following Channel-ID: {}", member.getEffectiveName(), vc.getIdLong());
        }
    }

    protected void onLeave(AudioChannel audioChannel) {

        if (audioChannel.getMembers().isEmpty()) {
            try (ResultSet set = LiteSQL.onQuery("SELECT channelId FROM createdprivatevcs;")) {

                while (set.next()) {
                    this.tempchannels.add(set.getLong("channelId"));
                }
                if (this.tempchannels.contains(audioChannel.getIdLong())) {

                    try (KAutoCloseable ignored = LoggingFilter.getInstance().blockEventExecution()) {
                        LoggingFilter.getInstance().getLoggingBlocker().block(audioChannel.getIdLong());
                        audioChannel.delete().queue();
                    }

                    LiteSQL.onUpdate("DELETE FROM createdprivatevcs WHERE channelId = ? AND guildId=?;",
                            audioChannel.getIdLong(), audioChannel.getGuild().getIdLong());
                    this.tempchannels.clear();
                    K7Bot.getInstance().getMainLogger().info("Removed custom VoiceChannel with the Name: {} and the following ID: {}", audioChannel.getName(), audioChannel.getIdLong());
                }

            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
