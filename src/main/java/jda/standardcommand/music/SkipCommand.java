package jda.standardcommand.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class SkipCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        int numb = 1;
        if (!args.isEmpty()) {
            final String s = args.get(0);
            try {
                numb = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                channel.sendMessage("Skipping one song, not an integer").queue();
            }
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
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel()) && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("You are not in the right voice channel!").queue();
            return;
        }


        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessage("There is no track currently playing!").queue();
            return;
        }
        ctx.getMessage().addReaction("✅").queue();

        musicManager.scheduler.nextTrack(numb);
        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessage("You have reached the end of the queue!").queue();
            return;
        }


    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp() {
        return "Skips the current track \n Usage: `?skip` / `?skip <position of queue>`";
    }
}
