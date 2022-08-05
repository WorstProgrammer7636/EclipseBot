package jda.standardcommand.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.lavaplayer.GuildMusicManager;
import jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class VolumeCommand implements ICommand {
    private final EventWaiter waiter;

    public VolumeCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

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

        if (volume > 175){
            cautionWarning(ctx, volume);
        } else {
            setVolume(ctx, volume);
        }


    }

    public void setVolume(CommandContext ctx, int volume){
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
        if (!selfVoiceState.getChannel().equals(memberVoiceState.getChannel())
                && audioManager.getConnectedChannel().getMembers().size() > 1) {
            channel.sendMessage("You are not in the right voice channel!").queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        musicManager.audioPlayer.setVolume(volume);
        channel.sendMessage("Volume of bot has been set to " + volume).queue();
    }

    public void cautionWarning(CommandContext ctx, int volume){
        final TextChannel channel = ctx.getChannel();
        channel.sendMessage("Beware! Setting the volume above 175 could cause noticeable earrape due to a large increase" +
                " in DB. Are you sure you want to proceed with this action?").
                setActionRows(ActionRow.of(
                        Button.secondary("1", "YES"),
                        Button.secondary("2", "NO"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                       if (e.getButton().getLabel().equalsIgnoreCase("YES")){
                           setVolume(ctx, volume);
                       } else if (e.getButton().getLabel().equalsIgnoreCase("NO")){
                           channel.sendMessage("Volume change cancelled").queue();
                           return;
                       }
                    },
                    15, TimeUnit.SECONDS,
                    () -> channel.sendMessage("You took too long to respond. Please try this command again").queue()
            );
        });
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
