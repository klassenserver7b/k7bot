package de.klassenserver7b.k7bot.logging.listeners;

import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static de.klassenserver7b.k7bot.util.ChannelUtil.getSystemChannel;

public class RoleLoggingListener extends ListenerAdapter {

    public RoleLoggingListener() {
        super();
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.ROLE_CREATE, event.getGuild())) {
            return;
        }

        StringBuilder gen = new StringBuilder();

        GuildMessageChannel system = getSystemChannel(event.getGuild());
        Role role = event.getRole();

        for (Permission perm : role.getPermissions()) {
            gen.append(perm.getName()).append(", ");
        }

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

        embbuild.setColor(Color.green);
        embbuild.setTitle("Role created: " + role.getName());
        embbuild.setDescription("**Role: ** " + role.getAsMention() + "\n **Permissions: **\n" + gen);

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.ROLE_DELETE, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getGuild());
        Role role = event.getRole();

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild());

        embbuild.setColor(Color.red);
        embbuild.setTitle("Role deleted: " + role.getName());
        embbuild.setDescription("**Role: ** " + role.getAsMention());

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onRoleUpdateName(@NotNull RoleUpdateNameEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.ROLE_NAME, event.getGuild())) {
            return;
        }

        StringBuilder gen = new StringBuilder();

        GuildMessageChannel system = getSystemChannel(event.getGuild());
        Role role = event.getRole();

        for (Permission perm : role.getPermissions()) {
            gen.append(perm.getName()).append(", ");
        }

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild().getIdLong());

        embbuild.setColor(Color.yellow);
        embbuild.setTitle("Role(**Name**) edited: " + role.getName());
        embbuild.setDescription("**Old Rolename: **" + event.getOldName()
                + "\n**New Rolename: **" + event.getNewName() + "\n **Permissions: **\n" + gen);

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onRoleUpdatePermissions(@NotNull RoleUpdatePermissionsEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.ROLE_PERMISSIONS, event.getGuild())) {
            return;
        }

        StringBuilder remove = new StringBuilder();
        StringBuilder added = new StringBuilder();
        StringBuilder gen = new StringBuilder();

        List<String> rem = new ArrayList<>();
        List<String> add = new ArrayList<>();

        event.getOldPermissions().forEach(perm -> {
            if (!event.getNewPermissions().contains(perm))
                rem.add(perm.getName());
        });

        for (Permission perm : event.getNewPermissions()) {
            gen.append(perm.getName()).append(", ");
            if (!event.getOldPermissions().contains(perm))
                add.add(perm.getName());
        }

        for (String s : rem) {
            remove.append(s).append(", ");
        }

        for (String s : add) {
            added.append(s).append(", ");
        }

        GuildMessageChannel system = getSystemChannel(event.getGuild());
        Role role = event.getRole();


        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild().getIdLong());

        embbuild.setColor(Color.yellow);
        embbuild.setTitle("Role(**Permissions**) edited: " + role.getName());
        embbuild.setDescription("**Role: **" + role.getAsMention() + "\n**Executive user: **UNKNOWN\n**Removed Permissions: **\n"
                + remove + "\n **Added Permissions: **\n" + added + "\n **Permissions: **\n" + gen);

        system.sendMessageEmbeds(embbuild.build()).queue();
    }

    @Override
    public void onRoleUpdatePosition(@NotNull RoleUpdatePositionEvent event) {

        if (LoggingConfigDBHandler.isOptionDisabled(LoggingOptions.ROLE_POSITION, event.getGuild())) {
            return;
        }

        GuildMessageChannel system = getSystemChannel(event.getGuild());

        EmbedBuilder embbuild = EmbedUtils.getDefault(event.getGuild().getIdLong());

        embbuild.setColor(Color.yellow);
        embbuild.setTitle("Role(**Position**) edited: " + event.getRole().getName());
        embbuild.setDescription("**Old Position: **" + event.getOldPosition() + "\n**New Position: **" + event.getNewPosition());

        system.sendMessageEmbeds(embbuild.build()).queue();
    }
}
