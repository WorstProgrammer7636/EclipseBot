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
        } else if (mentionedMembers.get(0).getIdLong() == Long.parseLong("1003478249755656273")){
            channel.sendMessage("Sorry but I can't really play with you! I'm juggling lots of programs right now. But maybe in the future " +
                    ":wink:").queue();
            return;
        } else if (mentionedMembers.get(0).getIdLong() == ctx.getAuthor().getIdLong()){
            channel.sendMessage("Really? You want to play with yourself? Why? No friends? Ok, be my guest :smile:").queue();
        }

        firstSelection(ctx, channel, mentionedMembers.get(0));
    }


    public void firstSelection(CommandContext ctx, TextChannel channel, Member opponent){
        channel.sendMessage(ctx.getAuthor().getName() + ", you have 10 seconds to choose between rock, paper, or scissors. Go!").
                setActionRows(ActionRow.of(
                        Button.success("1", "rock"),
                        Button.success("2", "paper"),
                        Button.success("3", "scissors"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        String firstSelectionChoice = e.getButton().getLabel();
                        secondSelection(ctx, channel, ctx.getAuthor().getName(), opponent, firstSelectionChoice);
                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Disqualified. You took too long to respond").queue()
            );
        });
    }

    public void secondSelection(CommandContext ctx, TextChannel channel, String protag, Member opponent, String first){
        channel.sendMessage(opponent.getEffectiveName() + ", Your Turn! You have 10 seconds to choose between rock, paper, or scissors. Go!").
                setActionRows(ActionRow.of(
                        Button.danger("1", "rock"),
                        Button.danger("2", "paper"),
                        Button.danger("3", "scissors"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == opponent.getIdLong();
                    },
                    (e) -> {
                        String secondSelectionChoice = e.getButton().getLabel();
                        displayWinner(ctx, channel, protag, opponent.getEffectiveName(), first, secondSelectionChoice);
                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Disqualified. You took too long to respond").queue()
            );
        });
    }

    public void displayWinner(CommandContext ctx, TextChannel channel, String protag, String opponent, String firstSelectionChoice, String secondSelectionChoice){
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
