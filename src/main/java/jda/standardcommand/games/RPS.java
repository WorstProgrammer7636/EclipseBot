package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RPS implements ICommand {
    private final EventWaiter waiter;
    String firstSelectionChoice;
    String secondSelectionChoice;

    public RPS(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();

        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if (mentionedMembers.size() == 0){
            channel.sendMessage("You didn't mention anyone to play with!").queue();
            return;
        } else if (mentionedMembers.size() > 1){
            channel.sendMessage("You can only play with one person").queue();
            return;
        }

        firstSelection(ctx, channel, mentionedMembers.get(0));
    }


    public void firstSelection(CommandContext ctx, TextChannel channel, Member opponent){
        channel.sendMessage(ctx.getAuthor().getName() + ", you have 10 seconds to choose between rock, paper, or scissors. Go!").
                setActionRows(ActionRow.of(
                        Button.secondary("1", "rock"),
                        Button.secondary("2", "paper"),
                        Button.secondary("3", "scissors"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        firstSelectionChoice = e.getButton().getLabel();
                        channel.sendMessage(ctx.getAuthor().getName() + " has locked in his choice!").queue();
                        secondSelection(ctx, channel, ctx.getAuthor().getName(), opponent);
                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Disqualified. You took too long to respond").queue()
            );
        });
    }

    public void secondSelection(CommandContext ctx, TextChannel channel, String protag, Member opponent){
        channel.sendMessage(opponent.getEffectiveName() + ", Your Turn! You have 10 seconds to choose between rock, paper, or scissors. Go!").
                setActionRows(ActionRow.of(
                        Button.secondary("1", "rock"),
                        Button.secondary("2", "paper"),
                        Button.secondary("3", "scissors"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == opponent.getIdLong();
                    },
                    (e) -> {
                        secondSelectionChoice = e.getButton().getLabel();
                        channel.sendMessage(opponent.getEffectiveName() + " has locked in his choice!").queue();
                        displayWinner(ctx, channel, protag, opponent.getEffectiveName());
                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Disqualified. You took too long to respond").queue()
            );
        });
    }

    public void displayWinner(CommandContext ctx, TextChannel channel, String protag, String opponent){
        String finalMessage = protag + " picked " + firstSelectionChoice + ". " +
                opponent + " picked " + secondSelectionChoice + ". ";
        if (firstSelectionChoice.equals("rock") && secondSelectionChoice.equals("rock")){
            channel.sendMessage(finalMessage + "IT IS A TIE!").queue();
        } else if (firstSelectionChoice.equals("rock") && secondSelectionChoice.equals("paper")){
            channel.sendMessage(finalMessage + opponent + " WINS!").queue();
        } else if (firstSelectionChoice.equals("rock") && secondSelectionChoice.equals("scissors")){
            channel.sendMessage(finalMessage + protag + " WINS!").queue();
        } else if (firstSelectionChoice.equals("paper") && secondSelectionChoice.equals("rock")){
            channel.sendMessage(finalMessage + protag + " WINS!").queue();
        } else if (firstSelectionChoice.equals("paper") && secondSelectionChoice.equals("paper")){
            channel.sendMessage(finalMessage + "IT IS A TIE!").queue();
        } else if (firstSelectionChoice.equals("paper") && secondSelectionChoice.equals("scissors")){
            channel.sendMessage(finalMessage + opponent + " WINS!").queue();
        } else if (firstSelectionChoice.equals("scissors") && secondSelectionChoice.equals("rock")){
            channel.sendMessage(finalMessage + opponent + " WINS!").queue();
        } else if (firstSelectionChoice.equals("scissors") && secondSelectionChoice.equals("paper")){
            channel.sendMessage(finalMessage + protag + " WINS!").queue();
        } else if (firstSelectionChoice.equals("scissors") && secondSelectionChoice.equals("scissors")){
            channel.sendMessage(finalMessage + "IT IS A TIE!").queue();
        }
        return;

    }

    @Override
    public String getName() {
        return "rps";
    }

    @Override
    public String getHelp() {
        return "play rock paper scissors with someone. Usage/Example: ?rps @user";
    }
}
