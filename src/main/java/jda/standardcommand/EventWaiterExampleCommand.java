package jda.standardcommand;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EventWaiterExampleCommand implements ICommand {
    private static String EMOTE = "\uD83D\uDE33";
    private final EventWaiter waiter;
    public EventWaiterExampleCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        channel.sendMessage("React with").append(EMOTE).append("!!!").queue((message) -> {
            message.addReaction(EMOTE).queue();

            this.waiter.waitForEvent(
                    GuildMessageReactionAddEvent.class,
                    (e) -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(),
                    (e) -> {
                        channel.sendMessageFormat("WOw %#s You are so cool!!!!", e.getUser()).queue();
                    },
                    5, TimeUnit.SECONDS,
                    () -> channel.sendMessage("You're too slow You're too slow HAHAHAHAHA").queue()
            );
        });
    }

    @Override
    public String getName() {
        return "eventwaiter";
    }

    @Override
    public String getHelp() {
        return "Eventwaiter Example Command, Pretty Useless LOL. \n Usage: `?eventwaiter`";
    }
}
