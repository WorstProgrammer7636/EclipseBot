package jda.standardcommand.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueCommand implements ICommand {
    public static String left = "◀";
    public static String right = "▶";
    public static String pause = "⏯";
    private final EventWaiter waiter;

    public QueueCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) throws IOException {
        waiter(null, 0, true, ctx);
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
        return String.format("%02d:%02d:%02d", hours, minutes - 60 * hours, seconds);
    }

    public void waiter(Message message, int start, boolean og, CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final AudioTrack playingTrack = musicManager.audioPlayer.getPlayingTrack();
        if (og) {
            if (queue.isEmpty() && playingTrack == null) {
                //No Songs Playing
                channel.sendMessage("The queue is currently empty, and there is no track playing!").queue();
                return;
            }
            //Variables
            final int trackCount = Math.min(queue.size(), 10);
            final List<AudioTrack> trackList = new ArrayList<>(queue);
            EmbedBuilder info;
            long nowparsed = (playingTrack.getPosition());
            long totalparsed = (playingTrack.getDuration());
            String total = formatTime(totalparsed - nowparsed);

            StringBuilder builder = new StringBuilder();
            //Paused
            if (musicManager.audioPlayer.isPaused()) {
                builder.append("**The Player Is Paused**\n \n");
            }
            //Now Playing
            builder.append(String.format("**Now Playing**: \n [`%s`](%s) by `%s` \n Remaining playtime: `[%s]` \n \n",
                    playingTrack.getInfo().title,
                    playingTrack.getInfo().uri,
                    playingTrack.getInfo().author, total));
            builder.append("**Queue:** \n");
            //Repeating Song On
            if (musicManager.scheduler.repeating) {
                builder.append("**This song is repeating!** \n Use the repeat command to cancel the repeat! \n");
                info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
                info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                        musicManager.scheduler.queuerepeating ? "✅" : "❌"
                        , musicManager.scheduler.repeating ? "✅" : "❌"));

            } if (!queue.isEmpty()) {
                //Normal Queue
                for (int i = 0; i < trackCount; i++) {

                    final AudioTrack track = trackList.get(i);
                    final AudioTrackInfo trackInfo = track.getInfo();
                    long cparsed = (track.getDuration());
                    String ttotal = formatTime(cparsed);

                    builder.append("**").append((i + 1)).append(String.format(". [`%s`](%s)** by `%s`  *`[%s]`*\n",
                            trackInfo.title, trackInfo.uri, trackInfo.author, ttotal));
                }
            } else {
                //Queue Repeating, but no songs queued
                if (musicManager.scheduler.queuerepeating) {
                    builder.append("""
                            **This song is repeating (using the queue loop)!**\s
                             Use the queueloop command to cancel the repeat!\s
                             There are `""")
                            .append(0)
                            .append("` other songs in the queue that will start if the repeat is disabled.");
                    info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
                    info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                            musicManager.scheduler.queuerepeating ? "✅" : "❌"
                            , musicManager.scheduler.repeating ? "✅" : "❌"));

                    int finalStart = start;
                    channel.sendMessageEmbeds(info.build()).queue((curr) -> {
                        curr.addReaction(right).queue();
                        curr.addReaction(pause).queue();


                        this.waiter.waitForEvent(
                                GuildMessageReactionAddEvent.class,
                                (e) -> e.getMessageIdLong() == curr.getIdLong() && !e.getUser().isBot(),
                                (e) -> {
                                    if(e.getReactionEmote().getName().equals(left)) {
                                        waiter(curr, finalStart - 10, false, ctx);
                                    } else if(e.getReactionEmote().getName().equals(right)) {
                                        waiter(curr, finalStart + 10, false, ctx);
                                    } else if(e.getReactionEmote().getName().equals(pause)) {
                                        musicManager.audioPlayer.setPaused(!musicManager.audioPlayer.isPaused());
                                        waiter(curr,finalStart, false, ctx);
                                    }
                                    else {
                                        curr.clearReactions().queue();
                                    }
                                },
                                30, TimeUnit.SECONDS,
                                () -> curr.clearReactions().queue()
                        );
                    });
                    return;
                }
                //Empty Queue
                builder.append("Empty Queue!");
            }
            //Add songs
            if (trackList.size() - start > trackCount) {
                builder.append(String.format("And `%s` more songs!", trackList.size() - trackCount));
            }
            //Normal Queue
            info = EmbedUtils.embedMessageWithTitle("Current Queue:", "" + builder);
            info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                    musicManager.scheduler.queuerepeating ? "✅" : "❌"
                    , musicManager.scheduler.repeating ? "✅" : "❌"));


            int finalStart = start;
            channel.sendMessageEmbeds(info.build()).queue((curr) -> {
                curr.addReaction(right).queue();
                curr.addReaction(pause).queue();

                this.waiter.waitForEvent(
                        GuildMessageReactionAddEvent.class,
                        (e) -> e.getMessageIdLong() == curr.getIdLong() && !e.getUser().isBot(),
                        (e) -> {
                            if(e.getReactionEmote().getName().equals(left)) {
                                waiter(curr, finalStart - 10, false, ctx);
                            } else if(e.getReactionEmote().getName().equals(right)) {
                                waiter(curr, finalStart + 10, false, ctx);
                            } else if(e.getReactionEmote().getName().equals(pause)) {
                                musicManager.audioPlayer.setPaused(!musicManager.audioPlayer.isPaused());
                                waiter(curr,finalStart, false, ctx);
                            }
                            else {
                                curr.clearReactions().queue();
                            }
                        },
                        30, TimeUnit.SECONDS,
                        () -> curr.clearReactions().queue()
                );
            });

            //continuation
        } else {
            if(start <= 0) {
                start = 0;
            } else if(start >= queue.size()) {
                start = queue.size()-10;
            }
            if (queue.isEmpty() && playingTrack == null) {
                //No Songs Playing
                channel.sendMessage("The queue is currently empty, and there is no track playing!").queue();
                return;
            }
            //Variables
            final int trackCount = Math.min(queue.size()-start, 10);
            final List<AudioTrack> trackList = new ArrayList<>(queue);
            EmbedBuilder info;
            long nowparsed = (playingTrack.getPosition());
            long totalparsed = (playingTrack.getDuration());
            String total = formatTime(totalparsed - nowparsed);

            StringBuilder builder = new StringBuilder();
            //Paused
            if (musicManager.audioPlayer.isPaused()) {
                builder.append("**The Player Is Paused**\n \n");
            }
            //Now Playing
            builder.append(String.format("**Now Playing**: \n [`%s`](%s) by `%s` \n Remaining playtime: `[%s]` \n \n",
                    playingTrack.getInfo().title,
                    playingTrack.getInfo().uri,
                    playingTrack.getInfo().author, total));
            builder.append("**Queue:** \n");
            //Repeating Song On
            if (musicManager.scheduler.repeating) {
                builder.append("**This song is repeating!** \n Use the repeat command to cancel the repeat! \n");
                info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
                info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                        musicManager.scheduler.queuerepeating ? "✅" : "❌"
                        , musicManager.scheduler.repeating ? "✅" : "❌"));

            } if (!queue.isEmpty()) {
                //Normal Queue
                for (int i = start; i < trackCount + start; i++) {

                    final AudioTrack track = trackList.get(i);
                    final AudioTrackInfo trackInfo = track.getInfo();
                    long cparsed = (track.getDuration());
                    String ttotal = formatTime(cparsed);

                    builder.append("**").append((i + 1)).append(String.format(". [`%s`](%s)** by `%s`  *`[%s]`*\n",
                            trackInfo.title, trackInfo.uri, trackInfo.author, ttotal));
                }
            } else {
                //Queue Repeating, but no songs queued
                if (musicManager.scheduler.queuerepeating) {
                    builder.append("""
                            **This song is repeating (using the queue loop)!**\s
                             Use the queueloop command to cancel the repeat!\s
                             There are `""")
                            .append(0)
                            .append("` other songs in the queue that will start if the repeat is disabled.");
                    info = EmbedUtils.embedMessageWithTitle("Repeating Song:", "" + builder);
                    info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                            musicManager.scheduler.queuerepeating ? "✅" : "❌"
                            , musicManager.scheduler.repeating ? "✅" : "❌"));

                    int finalStart = start;
                    message.editMessageEmbeds(info.build()).queue((curr) -> {
                        curr.clearReactions().queue();
                        if(finalStart != 0) {
                            curr.addReaction(left).queue();
                        } if(finalStart <= queue.size() - 10) {
                            curr.addReaction(right).queue();
                        }
                        curr.addReaction(pause).queue();

                        this.waiter.waitForEvent(
                                GuildMessageReactionAddEvent.class,
                                (e) -> e.getMessageIdLong() == curr.getIdLong() && !e.getUser().isBot(),
                                (e) -> {
                                    if(e.getReactionEmote().getName().equals(left)) {
                                        waiter(curr, finalStart - 10, false, ctx);
                                    } else if(e.getReactionEmote().getName().equals(right)) {
                                        waiter(curr, finalStart + 10, false, ctx);
                                    } else if(e.getReactionEmote().getName().equals(pause)) {
                                        musicManager.audioPlayer.setPaused(!musicManager.audioPlayer.isPaused());
                                        waiter(curr,finalStart, false, ctx);
                                    }
                                    else {
                                        curr.clearReactions().queue();
                                    }
                                },
                                30, TimeUnit.SECONDS,
                                () -> curr.clearReactions().queue()
                        );
                    });
                    return;
                }
                //Empty Queue
                builder.append("Empty Queue!");
            }
            //Add songs
            if (trackList.size() - start > trackCount) {
                builder.append(String.format("And `%s` more songs!", trackList.size() - trackCount - start));
            }
            //Normal Queue
            info = EmbedUtils.embedMessageWithTitle("Current Queue:", "" + builder);
            info.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                    musicManager.scheduler.queuerepeating ? "✅" : "❌"
                    , musicManager.scheduler.repeating ? "✅" : "❌"));

            int finalStart = start;
            message.editMessageEmbeds(info.build()).queue((curr) -> {
                curr.clearReactions().queue();
                if(finalStart != 0) {
                    curr.addReaction(left).queue();
                } if(finalStart < queue.size() - 10) {
                    curr.addReaction(right).queue();
                }
                curr.addReaction(pause).queue();

                this.waiter.waitForEvent(
                        GuildMessageReactionAddEvent.class,
                        (e) -> e.getMessageIdLong() == curr.getIdLong() && !e.getUser().isBot(),
                        (e) -> {
                            if(e.getReactionEmote().getName().equals(left)) {
                                waiter(curr, finalStart - 10, false, ctx);
                            } else if(e.getReactionEmote().getName().equals(right)) {
                                waiter(curr, finalStart + 10, false, ctx);
                            } else if(e.getReactionEmote().getName().equals(pause)) {
                                musicManager.audioPlayer.setPaused(!musicManager.audioPlayer.isPaused());
                                waiter(curr,finalStart, false, ctx);
                            }
                            else {
                                curr.clearReactions().queue();
                            }
                        },
                        30, TimeUnit.SECONDS,
                        () -> curr.clearReactions().queue()
                );
            });
        }

    }

}