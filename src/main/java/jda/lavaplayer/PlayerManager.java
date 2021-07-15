package jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.client.config.RequestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackUrl, boolean isUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {

                play(musicManager, audioTrack);
                int count = musicManager.scheduler.queue.size();

                channel.sendMessage(
                        "Adding to queue `" + audioTrack.getInfo().title + "` by `"
                                + audioTrack.getInfo().author + "` (First search result found!) \n Position in Queue: `" + count + "`").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                if (isUrl) {
                    final List<AudioTrack> tracks = audioPlaylist.getTracks();
                    channel.sendMessage("Adding to queue: `").append(String.valueOf(tracks.size()))
                            .append("` tracks from the playlist `")
                            .append(audioPlaylist.getName()).append("`").queue();
                    StringBuilder builder = new StringBuilder();
                    EmbedBuilder info;
                    boolean built = false;

                    for (final AudioTrack track : tracks) {
                        play(musicManager, track);
                    }
                } else {
                    AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = audioPlaylist.getTracks().get(0);
                    }


                    play(musicManager, firstTrack);
                    int count = musicManager.scheduler.queue.size();

                    channel.sendMessage(
                            "Adding to queue " + firstTrack.getInfo().title + " by "
                                    + firstTrack.getInfo().author + " (First search result found!) \n Position in Queue: `" + count + "`").queue();
                }


            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();

            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Could not play: " + e.getMessage()).queue();

            }
        });
    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

}
