package jda.standardcommand.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final AudioTrack playingTrack = musicManager.audioPlayer.getPlayingTrack();


        if (queue.isEmpty() && playingTrack == null) {
            channel.sendMessage("The queue is currently empty, and there is no track playing!").queue();
            return;
        }

        final int trackCount = Math.min(queue.size(), 10);
        final List<AudioTrack> trackList = new ArrayList<>(queue);
        EmbedBuilder info = EmbedUtils.embedMessageWithTitle("Error:", "No songs in queue!");
        long nowparsed = (playingTrack.getPosition());
        long totalparsed = (playingTrack.getDuration());
        String total = formatTime(totalparsed - nowparsed);

        StringBuilder builder = new StringBuilder();
        if (musicManager.audioPlayer.isPaused()){
            builder.append("**The Player Is Paused**\n \n");
        }
        builder.append(String.format("**Now Playing**: \n [`%s`](%s) by `%s` \n Remaining playtime: [`%s`] \n \n",
                playingTrack.getInfo().title,
                playingTrack.getInfo().uri,
                playingTrack.getInfo().author, total));
        builder.append("**Queue:** \n");
        if (musicManager.scheduler.repeating) {
            builder.append("**This song is repeating!** \n Use the repeat command to cancel the repeat! \n There are `")
                    .append(queue.size())
                    .append("` other songs in the queue that will start if the repeat is disabled.");
            info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
            info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s", musicManager.scheduler.queuerepeating ? "✅" : "❌"
                    , musicManager.scheduler.repeating ? "✅" : "❌"));
            channel.sendMessageEmbeds(info.build()).queue();
            return;
        }
        if (!queue.isEmpty()) {
            for (int i = 0; i < trackCount; i++) {

                final AudioTrack track = trackList.get(i);
                final AudioTrackInfo trackInfo = track.getInfo();
                long cparsed = (track.getDuration());
                String ttotal = formatTime(cparsed);

                builder.append((i + 1)).append(String.format(". [`%s`](%s) by `%s`  [`%s`]\n \n", trackInfo.title, trackInfo.uri, trackInfo.author, ttotal));
            }
        } else {
            if (musicManager.scheduler.queuerepeating) {
                builder.append("**This song is repeating (using the queue loop)!** \n Use the queueloop command to cancel the repeat! \n There are `")
                        .append(0)
                        .append("` other songs in the queue that will start if the repeat is disabled.");
                info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
                info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s", musicManager.scheduler.queuerepeating ? "✅" : "❌"
                        , musicManager.scheduler.repeating ? "✅" : "❌"));
                channel.sendMessageEmbeds(info.build()).queue();
                return;
            }
            builder.append("Empty Queue!");
        }
        if (trackList.size() > trackCount) {
            builder.append(String.format("And `%s` more songs!", trackList.size() - trackCount));
        }

        info = EmbedUtils.embedMessageWithTitle("Current Queue:", "" + builder);
        info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s", musicManager.scheduler.queuerepeating ? "✅" : "❌"
        , musicManager.scheduler.repeating ? "✅" : "❌"));
        channel.sendMessageEmbeds(info.build()).queue();


    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getHelp() {
        return "Shows the queued songs \n Usage: `?queue`";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("q");
    }

    private String formatTime(long timeInMillis) {
        final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes-60*hours, seconds);
    }
}
