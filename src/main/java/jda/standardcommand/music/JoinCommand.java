package jda.standardcommand.music;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class JoinCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final AudioManager audioManager = ctx.getGuild().getAudioManager();

        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState.inVoiceChannel() && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("Already in a voice channel!").queue();
            return;
        }

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
        ctx.getMessage().addReaction("✅").queue();

    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getHelp() {
        return "Makes the bot join your voice channel \n Usage: `?join`";
    }
}
