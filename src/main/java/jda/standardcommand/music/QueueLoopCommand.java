package jda.standardcommand.music;

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
public class QueueLoopCommand implements ICommand {
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
        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("You are not in the voice channel!").queue();
            return;
        }
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel()) && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("You are not in the right voice channel!").queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final boolean newQueueRepeating = !musicManager.scheduler.queuerepeating;

        musicManager.scheduler.queuerepeating = newQueueRepeating;

        channel.sendMessageFormat("The queue has been set to **%s**", newQueueRepeating ? "repeating" : "not repeating").queue();
    }

    @Override
    public String getName() {
        return "queueloop";
    }

    @Override
    public String getHelp() {
        return "Loops the queue (queues song again after its done) \n Usage: `?queueloop`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("loopqueue", "qloop", "loopq", "ql", "lq");
    }
}
