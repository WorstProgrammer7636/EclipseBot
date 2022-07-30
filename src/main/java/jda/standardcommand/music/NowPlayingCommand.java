package jda.standardcommand.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public class NowPlayingCommand implements ICommand {
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

        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        final AudioTrack playingTrack = audioPlayer.getPlayingTrack();


        if (playingTrack == null) {
            channel.sendMessage("There is no track currently playing!").queue();
            return;
        }

        long nowparsed = (playingTrack.getPosition());
        String now = formatTime(nowparsed);


        long totalparsed = (playingTrack.getDuration());
        String total = formatTime(totalparsed);

        final AudioTrackInfo info = playingTrack.getInfo();
        StringBuilder builder = new StringBuilder();
        double frac = ((double) nowparsed / totalparsed);

        builder.append("\n \n");
        if (musicManager.audioPlayer.isPaused()) {
            builder.append("**The Player Is Paused**\n \n");
        }
        EmbedBuilder embedBuilder;
        builder.append(String.format("[`%s`](%s) by `%s` \n Position: `%s / %s` \n \n ",
                info.title, info.uri, info.author, now, total));
        for(double i = 0.0; i < 18; i++) {
            if(frac > i/18.0) {
                if((i+1)/18 > frac) {
                    builder.append(" :black_large_square:");
                } else {
                    builder.append(" :blue_square:");
                }
            } else {
                builder.append(" :black_square_button:");
            }
        }
        ctx.getMessage().addReaction("✅").queue();
        if (musicManager.scheduler.repeating) {
            builder.append(" \n **This song is repeating!** \n Use the repeat command to cancel the repeat! \n");
        }
        embedBuilder = EmbedUtils.embedMessageWithTitle("Now Playing:", "" + builder);
        embedBuilder.setFooter(String.format("Inutile || Queue Recycling: %s Song Repeat: %s",
                musicManager.scheduler.queuerepeating ? "✅" : "❌"
                , musicManager.scheduler.repeating ? "✅" : "❌"));

        EmbedBuilder finalInfo = embedBuilder;

        WebUtils.ins.getJSONObject("https://youtube.googleapis.com/youtube/v3/videos?part=snippet&id=" +
                musicManager.audioPlayer.getPlayingTrack().getIdentifier() + "&key="
                + Config.get("youtubeapitoken")).async((json) -> {

            final String url = json.findValue("medium").get("url").asText();
            finalInfo.setThumbnail(url);
            channel.sendMessageEmbeds(finalInfo.build()).queue();
        });


    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getHelp() {
        return "Shows the song that is currently playing \n Usage: `?nowplaying`";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("np");

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