package jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;
    public boolean queuerepeating = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public void nextTrack(int skipped) {
        AudioTrack track = null;
        boolean first = true;

        for (int i = 0; i < skipped; i++) {
            if (this.queuerepeating) {
                if(first) {
                    AudioTrack currentTrack = player.getPlayingTrack();
                    if (currentTrack != null) {
                        queue(currentTrack.makeClone());
                    }
                    first = false;
                } else {
                    queue(track);
                }
            }
            track = this.queue.poll();
        }
        try {
            this.player.startTrack(track, false);
        } catch (FriendlyException e) {
            this.player.startTrack(track, false);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (this.repeating) {
                this.player.startTrack(track.makeClone(), false);
                return;
            } else if (this.queuerepeating) {
                queue(track.makeClone());
            }
            nextTrack(1);
        }
    }


}
