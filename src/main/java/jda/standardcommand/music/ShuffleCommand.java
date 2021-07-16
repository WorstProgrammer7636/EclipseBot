package jda.standardcommand.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;

@SuppressWarnings("ConstantConditions")
public class ShuffleCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();


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
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel()) && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("You are not in the right voice channel!").queue();
            return;
        }
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You are not in the voice channel!").queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        if(queue.isEmpty()) {
            channel.sendMessage("Nothing to shuffle!").queue();
            return;
        }
        ArrayList<AudioTrack> list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        for(AudioTrack track : list) {
            queue.offer(track);
        }
        musicManager.scheduler.queue = queue;
        ctx.getMessage().addReaction("✅").queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp() {
        return "Shuffles the current queue \n Usage: `?shuffle`";
    }
}