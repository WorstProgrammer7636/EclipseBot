package jda.standardcommand.music;

import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class PlayCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage("Correct usage is `?play <youtube link/search>`").queue();
            return;
        }
        final Member self = ctx.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        final AudioManager audioManager = ctx.getGuild().getAudioManager();

        if (!selfVoiceState.inVoiceChannel()) {
            final Member member = ctx.getMember();
            final GuildVoiceState memberVoiceState = member.getVoiceState();

            if (!memberVoiceState.inVoiceChannel()) {
                channel.sendMessage("You are not in a voice channel!").queue();
                return;
            }
            final VoiceChannel memberChannel = memberVoiceState.getChannel();
            if (!self.hasPermission(memberChannel, Permission.VOICE_CONNECT)) {
                channel.sendMessage("Please give me permission to join that voice channel!").queue();
                return;
            }
            audioManager.openAudioConnection(memberChannel);
            channel.sendMessageFormat("Connecting to `\uD83D\uDD0A %s`", memberChannel.getName()).queue();
            String link = String.join(" ", ctx.getArgs());
            if (!isUrl(link)) {
                link = "ytsearch: " + link;
                PlayerManager.getInstance().loadAndPlay(channel,
                        link, false);
                return;
            }
            PlayerManager.getInstance().loadAndPlay(channel,
                    link, true);
            return;
        }
        selfVoiceState = self.getVoiceState();
        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel()) && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("Already in a voice channel!").queue();
            return;
        }
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You are not in the voice channel!").queue();
            return;
        }

        final VoiceChannel memberChannel = memberVoiceState.getChannel();
        if (!self.hasPermission(memberChannel, Permission.VOICE_CONNECT)) {
            channel.sendMessage("Please give me permission to join that voice channel!").queue();
            return;
        }
        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            audioManager.openAudioConnection(memberChannel);
            channel.sendMessageFormat("Connecting to `\uD83D\uDD0A %s`", memberChannel.getName()).queue();
        }

        String link = String.join(" ", ctx.getArgs());
        if (!isUrl(link)) {
            link = "ytsearch: " + link;
            PlayerManager.getInstance().loadAndPlay(channel,
                    link, false);
            return;
        }
        PlayerManager.getInstance().loadAndPlay(channel,
                link, true);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getHelp() {
        return "Plays a song\n Usage: `?play <youtube link>`";
    }

    public boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public List<String> getAliases() {
        return Collections.singletonList("p");
    }

}
