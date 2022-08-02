package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 1) Ask user to mention all ppl they want to play with (mention no ppl if they want to play by themselves)
 * 2) Ask user if they want random word generated or if they want to make their own word
 *      If user wants random generated: Generate via API
 *      If user wants own word: Make bot private DM them to ask for the word
 *
 *
 */
public class Hangman implements ICommand {
    ArrayList<Member> players = new ArrayList<Member>();
    private final EventWaiter waiter;
    String prompt;
    int incorrect = 0;
    ArrayList<String> alreadyGuessedLetters = new ArrayList<>();
    StringBuilder hangmanLetters = new StringBuilder();

    public Hangman(EventWaiter waiter) {
        this.waiter = waiter;
    }
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        players.clear();
        prompt = "";
        incorrect = 0;
        alreadyGuessedLetters.clear();
        hangmanLetters = new StringBuilder();
        TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();

        getPlayerQuantity(ctx);
    }

    public void getPlayerQuantity(CommandContext ctx){
        TextChannel channel = ctx.getChannel();
        channel.sendMessage("Welcome to hangman! Please mention/ping anyone you would like to play with. For example, if" +
                " you wanted to play with your friends Bob and Joe, type in chat '@Bob and @Joe'" +
                "If you want to play by yourself, just simply type in chat 'myself'").queue((message) -> {

            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        List<Member> mentionedMembers = e.getMessage().getMentionedMembers();
                        if (mentionedMembers.size() == 0 || e.getMessage().getContentRaw().equalsIgnoreCase("myself")){
                            channel.sendMessage("You chose to play by yourself!").queue();
                            players.add(ctx.getMember());
                        } else if (mentionedMembers.size() > 0){
                            players.add(ctx.getMember());
                            players.addAll(mentionedMembers);
                        }
                        getTargetWord(ctx);
                        System.out.println(players);
                    },
                    45, TimeUnit.SECONDS,
                    () -> {
                        channel.sendMessage("You took too long to respond. Please try this command again").queue();
                        return;
                    }
            );
        });
    }

    public void getTargetWord(CommandContext ctx){
        TextChannel channel = ctx.getChannel();
        channel.sendMessage("Cool. Now that we've gotten all users you want to play with, let me know if you " +
                "would like to have a randomly generated word or if you would like to choose your own custom word!").setActionRows(
                        ActionRow.of(Button.secondary("random", "Generate Random Word"),
                                Button.secondary("custom", "Use Custom Word"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        if (e.getButton().getId().equals("random")){
                            //generate word via API (Add feature later)
                            channel.sendMessage("This feature is not available yet!").queue();

                        } else if (e.getButton().getId().equals("custom")){
                            //private DM user for custom word
                            User user = ctx.getMember().getUser();
                            String content = "Please enter your custom word here in this private DM";
                            DMUser(ctx, user, content);
                        }
                    },
                    90, TimeUnit.SECONDS,
                    () -> {channel.sendMessage("You took too long to respond. Please try this command again").queue();
                    return;}
            );
        });
    }

    public void DMUser(CommandContext ctx, User user, String content){
        TextChannel channel = ctx.getChannel();
        try {
            user.openPrivateChannel().queue((DM) ->
            {
                DM.sendMessage(content).queue(message -> {
                    this.waiter.waitForEvent(
                            PrivateMessageReceivedEvent.class,
                            (e) -> {
                                long nchannel = e.getChannel().getIdLong();
                                return DM.getIdLong() == nchannel && !e.getAuthor().isBot();
                            },
                            (e) -> {
                                //Disregard any additional whitespace in user input
                                prompt = e.getMessage().getContentRaw().toLowerCase();
                                prompt = prompt.replaceAll("\\s+", " ");

                                //make sure there are no numbers or symbols in input
                                for (int letter = 0; letter < prompt.length(); letter++){
                                    if (((int) prompt.charAt(letter) < 97 || (int) prompt.charAt(letter) > 122) && prompt.charAt(letter) != ' '){
                                        DM.sendMessage("Please don't include numbers or other special characters in your input." +
                                                " Please run the hangman command to try again").queue();
                                        return;
                                    }

                                    if (prompt.charAt(letter) == ' '){
                                        hangmanLetters.append(' ');
                                    } else {
                                        hangmanLetters.append('-');
                                    }
                                }

                                System.out.println("Prompt is: " + prompt);

                                DM.sendMessage("Thanks for choosing your prompt.").queue();
                                channel.sendMessage("The game of hangman is now ongoing. Only people mentioned/pinged by the host are allowed to input" +
                                        " letters for this game. Anyone else's input will not be accepted." +
                                        " Finally, please input only ***one letter from a-z*** in the chat.").queue();

                                printDiagram(ctx);
                                initiateGame(ctx);
                            },
                            90, TimeUnit.SECONDS,
                            () -> {
                                channel.sendMessage("You took too long to respond. Please try this command again").queue();
                                return;
                            }
                    );
                });

            });
        } catch (Exception exception){
            channel.sendMessage("Bot was unable to DM host for custom word. Please turn on your DMs or" +
                    " allow messages from server members and try the hangman command again").queue();
            return;
        }
    }

    //utilize recursive waiter event until game ends by win or loss
    public void initiateGame(CommandContext ctx){
        TextChannel channel = ctx.getChannel();
            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        Member member = e.getMember();

                        return ctx.getChannel().getIdLong() == nchannel && players.contains(member);
                    },
                    (e) -> {
                        //game
                        String input = e.getMessage().getContentRaw().toLowerCase();
                        if ((input.length() > 1) || (int) input.charAt(0) < 97 || (int) input.charAt(0) > 122){
                            channel.sendMessage("That is not a valid input. Please try again and input only one letter from a-z.").queue();
                        } else {
                            checkForLetterInPrompt(ctx, input);
                            printDiagram(ctx);

                            //check for win
                            if (prompt.equalsIgnoreCase(String.valueOf(hangmanLetters))){
                                channel.sendMessage("Congratulations! You WON!!!! :smile: :partying_face:").queue();
                                return;
                            }

                            if (incorrect == 7){
                                channel.sendMessage("You died! L + bozo + The word was " + prompt).queue();
                                return;
                            }
                        }
                        initiateGame(ctx);

                    },
                    100, TimeUnit.SECONDS,
                    () -> {channel.sendMessage("You took too long to respond. Please try this command again").queue();
                        return;}
            );


    }

    public void checkForLetterInPrompt(CommandContext ctx, String input){
        ArrayList<Integer> characterOccurences = new ArrayList<>();
        if (prompt.contains(input)){
            ctx.getChannel().sendMessage("Correct guess!").queue();
            for (int i = 0; i < prompt.length(); i++){
                if (prompt.charAt(i) == input.charAt(0)){
                    characterOccurences.add(i);
                }
            }

            for (int j = 0; j < characterOccurences.size(); j++){
                int temp = characterOccurences.get(j);
                hangmanLetters.replace(temp, temp + 1, String.valueOf(prompt.charAt(temp)));
            }
        } else {
            ctx.getChannel().sendMessage("Incorrect guess!").queue();
            incorrect++;
        }

        alreadyGuessedLetters.add(input);
    }

    /**
     * Discord UI
     * @param ctx
     */
    public void printDiagram(CommandContext ctx){
        //set up hangman diagram
        String diagram = "+ - - +" +
                "\n|" +
                "\n|" +
                "\n|" +
                "\n|" +
                "\n|" +
                "\n=====";

        if (incorrect == 0){
            diagram = diagram;
        } else if (incorrect == 1){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|" +
                    "\n|" +
                    "\n|" +
                    "\n|" +
                    "\n=====";
        } else if (incorrect == 2){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|" +
                    "\n|" +
                    "\n|" +
                    "\n=====";
        } else if (incorrect == 3){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|" +
                    "\n=====";
        } else if (incorrect == 4){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E /|" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|" +
                    "\n=====";
        } else if (incorrect == 5){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E /|\\" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|" +
                    "\n=====";
        } else if (incorrect == 6){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E /|\\" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E / " +
                    "\n=====";
        } else if (incorrect == 7){
            diagram = "| - - - |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E :face_with_spiral_eyes:" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E /|\\" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E \u200E |" +
                    "\n|\u200E \u200E \u200E \u200E \u200E \u200E \u200E /\\" +
                    "\n=====";
        }
        EmbedBuilder hangman = new EmbedBuilder();
        hangman.addField("Your Hangman", diagram, true);
        hangman.addField("Guessed Letters", String.valueOf(alreadyGuessedLetters), true);
        hangman.addField("Letters", "\n \n" + hangmanLetters, true);
        hangman.setColor(Color.GREEN);
        ctx.getChannel().sendMessage(hangman.build()).queue();
    }


    @Override
    public String getName() {
        return "hangman";
    }

    @Override
    public String getHelp() {
        return "play hangman with your friends or yourself. Usage: ?hangman";
    }
}
