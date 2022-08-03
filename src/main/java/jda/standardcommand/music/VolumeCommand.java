package jda.standardcommand.music;

import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class VolumeCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        final TextChannel channel = ctx.getChannel();
        int volume = 0;
        if (ctx.getArgs().isEmpty()){
            channel.sendMessage("Please input a volume. Usage: ?volume <value between 1 and 100>").queue();
            return;
        }

        if (ctx.getArgs().size() > 1){
            channel.sendMessage("Not a valid input").queue();
            return;
        }

        try {
            volume = Integer.parseInt(ctx.getArgs().get(0));
        } catch (NumberFormatException e){
            channel.sendMessage("Please input an integer value").queue();
            return;
        }

        final Member self = ctx.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        final AudioManager audioManager = ctx.getGuild().getAudioManager();

        if (!selfVoiceState.inVoiceChannel()) {
            channel.sendMessage("I'm not in a voice channel!").queue();
            return;
        }
        selfVoiceState = self.getVoiceState();
        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You are not in the voice channel!").queue();
            return;
        }
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel())
                && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("You are not in the right voice channel!").queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        musicManager.audioPlayer.setVolume(volume);
        channel.sendMessage("Volume of bot has been sent to " + volume).queue();
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getHelp() {
        return "set volume for bot when playing audio";
    }
}
