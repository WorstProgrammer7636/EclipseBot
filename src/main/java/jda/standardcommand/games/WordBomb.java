package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class WordBomb implements ICommand {
    private final EventWaiter waiter;
    final String[] twoLetterWordPrompts =
            {"A", "AD", "AG", "AL", "AM", "AN", "AP", "AR", "AF", "AS", "AT", "BO", "CA", "CO", "CU", "DE", "DI", "EA", "EB", "EC", "ED", "EL", "EM", "EN", "ER", "ES", "ET",
                    "HE", "HI", "HO", "I", "IC", "ID", "IL", "IN", "IS", "L", "LA", "LE", "LI", "LO", "LU", "MA", "ME", "MI", "MO", "NA", "NE", "NU", "OF", "OM", "ON", "OR", "PA", "PH", "RA",
                    "RI", "RO", "SE", "TA", "TE", "TG", "TH", "TI", "TO", "XA", "KA", "WA", "TL", "XA"};
    final String[] threeLetterWordPrompts = {"ONO", "ILI", "AKI", "ATE", "ATI", "ATO", "ENA", "ENO", "ERO", "ETI", "IRA", "OCO", "OHA", "OPI", "REE", "RET", "SAP", "TER", "TIN", "UGI", "URI", "VAC", "URU", "CAN"};
    ArrayList<Long> players = new ArrayList<Long>();
    ArrayList<String> usedWords = new ArrayList<String>();
    List<User> invitedPlayers;
    ArrayList<String> actualInvitedPlayers = new ArrayList<String>();
    int[] playerLives;
    int turnsElapsed = 0;
    int timeForResponse = 10000;
    boolean shootThreeWords = false;
    GuildMessageReceivedEvent globalE;
    String prompt = "";
    String errorReason1 = "That word has already been typed by another user. You will be instantly eliminated if you fail to type an existing word this time.";
    String errorReason2 = "That word doesn't exist. You will be instantly eliminated if you fail to type an existing word this time.";
    String errorReason3 = "You didn't respond in time.";
    String errorReason4 = "Your word didn't contain the prompt letters";
    Random rand = new Random();
    public WordBomb(EventWaiter waiter){
        this.waiter = waiter;
    }

    public void starting(CommandContext ctx, TextChannel channel){
        channel.sendMessage("You started a game of word bomb. Would you like it to be public or private?").queue((message) -> {

            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        players.clear();
                        players.add(e.getAuthor().getIdLong());
                        actualInvitedPlayers.clear();
                        if (e.getMessage().getContentRaw().equalsIgnoreCase("public")){
                            channel.sendMessage("This option is still undergoing development, please select the private option").queue();
                            return;
                        } else if (e.getMessage().getContentRaw().equalsIgnoreCase("private")){
                            privateStart(ctx, channel);
                        } else {
                            channel.sendMessage("That isn't a valid response. Please try this command again.").queue();
                            return;
                        }

                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("You took too long to respond. Please try this command again.").queue()
            );
        });
    }

    public void privateStart(CommandContext ctx, TextChannel channel){
        channel.sendMessage("You started a private game of word bomb. Please ping everybody you want to play with to send them an invite. " +
                "They must respond with ACCEPT in the chat to accept your invite").queue((message) -> {
            this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> {
                long nchannel = e.getChannel().getIdLong();
                long nuser = e.getMember().getUser().getIdLong();
                return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
            }, e -> {
                invitedPlayers = e.getMessage().getMentionedUsers();
                for (User invitedPlayer : invitedPlayers) {
                    actualInvitedPlayers.add(String.valueOf(invitedPlayer));
                }
                if (invitedPlayers.contains(e.getAuthor())){
                    channel.sendMessage("Hey, you're already in the game. You can't invite yourself!").queue();
                    return;
                } //also make sure bot isn't pinged because bot can't play

                channel.sendMessage("You invited " + invitedPlayers.size() + " people to your game").queue();
                waitForPlayers(ctx, channel);
            }, 30, TimeUnit.SECONDS, () ->
                    channel.sendMessage("You took too long to respond. Please try this command again").queue());
        });
    }

    public void waitForPlayers(CommandContext ctx, TextChannel channel){
        if (actualInvitedPlayers.size() == 0){
            channel.sendMessage("Everyone has accepted. Getting started").queue();
            playerLives = new int[players.size()];
            Arrays.fill(playerLives, 3);
            game(ctx, channel);
            return;
        }

        this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e ->
                        actualInvitedPlayers.contains(String.valueOf(e.getAuthor())) && e.getMessage().getContentRaw().equalsIgnoreCase("ACCEPT"),
                e -> {
                    channel.sendMessage("A player joined").queue();
                    players.add(e.getAuthor().getIdLong());
                    actualInvitedPlayers.remove(String.valueOf(e.getAuthor()));
                    waitForPlayers(ctx, channel);
                },15, TimeUnit.SECONDS, () ->
                        channel.sendMessage("The next person took too long to accept, please try the command again").queue());
    }

    public StringBuffer fetchAPI(String input){
        StringBuffer response = new StringBuffer();
        try {
            String url = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" + input + "?key=66f50df9-f4b7-4a27-a058-30f3c0098d2e";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            in.close();
        } catch (IOException pp){
            pp.printStackTrace();
            System.exit(1);
        }
        return response;
    }

    public StringBuffer fetchAPI2(String input){
        StringBuffer response2 = new StringBuffer();
        try {
            String url2 = "https://www.dictionaryapi.com/api/v3/references/medical/json/" + input + "?key=4dcf350b-7351-4f26-ba8a-ffe0824b415d";
            URL obj2 = new URL(url2);
            HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();
            BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
            String inputLine2;
            while ((inputLine2 = in2.readLine()) != null){
                response2.append(inputLine2);
            }
            in2.close();
        } catch (IOException pp){
            pp.printStackTrace();
            System.exit(1);
        }
        return response2;
    }

    public boolean wordIsUsed(String input){
        if (usedWords.contains(input)){
            return true;
        }

        return false;
    }

    public void checkIfWordIsValid(String input, CommandContext ctx, TextChannel channel, GuildMessageReceivedEvent e){

    }

    public void game(CommandContext ctx, TextChannel channel) {
        if (players.size() == 1){
            channel.sendMessage("The game has ended").queue();
            return;
        }
        if (timeForResponse <= 4500){
            shootThreeWords = true;
            timeForResponse = 10000;
        }
        if (shootThreeWords) {
            prompt = threeLetterWordPrompts[rand.nextInt(threeLetterWordPrompts.length)];
        } else {
            prompt = twoLetterWordPrompts[rand.nextInt(twoLetterWordPrompts.length)];
        }


        double timeForResponseSeconds = (double) timeForResponse / 1000;
        String currentUsersTurn = "It is <@" + players.get(turnsElapsed % players.size()) + ">'s turn\n" +
                "Time left: " + timeForResponseSeconds + " seconds\n" +
                "Prompt: " + prompt;
        channel.sendMessage(currentUsersTurn).queue();
        this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> {
                    long nchannel = e.getChannel().getIdLong();
                    long nuser = e.getMember().getUser().getIdLong();
                    return ctx.getChannel().getIdLong() == nchannel && nuser == players.get(turnsElapsed % players.size());
                }, e -> {
                    globalE = e;
                    String input = e.getMessage().getContentRaw();
                    StringBuffer response = fetchAPI(input);
                    StringBuffer response2 = fetchAPI2(input);

                    try {
                        if (wordIsUsed(input)){
                            secondInputAttempt(ctx, channel, e, errorReason1);
                        } else if (response.toString().startsWith("meta", 3) || response2.toString().startsWith("meta", 3)){
                            if (input.contains(prompt) || input.contains(prompt.toLowerCase())){
                                channel.sendMessage("Success. That word exists").queue();
                                usedWords.add(input);
                                if (turnsElapsed % players.size() == players.size() - 1){
                                    timeForResponse -= 500;
                                }
                                turnsElapsed++;
                                game(ctx, channel);
                            } else {
                                secondInputAttempt(ctx, channel, e, errorReason4);
                            }

                        } else {
                            secondInputAttempt(ctx, channel, e, errorReason2);
                        }
                    } catch (Exception pp){
                        secondInputAttempt(ctx, channel, e, errorReason2);
                    }
                },
                timeForResponse, TimeUnit.MILLISECONDS, () ->
                {
                    secondInputAttempt(ctx, channel, globalE, errorReason3);
                });
    }

    public void secondInputAttempt(CommandContext ctx, TextChannel channel, GuildMessageReceivedEvent event, String errorReason){
        double timeForResponseSeconds2 = (double) timeForResponse / 1000;
        channel.sendMessage(errorReason + " Try again and type another word in the next " + timeForResponseSeconds2 + " seconds").queue();
        this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> {
            long nchannel = e.getChannel().getIdLong();
            long nuser = e.getMember().getUser().getIdLong();
            return nuser == players.get(turnsElapsed % players.size());
        }, e -> {
            String input = e.getMessage().getContentRaw();
            try {
                String url = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" + input + "?key=66f50df9-f4b7-4a27-a058-30f3c0098d2e";
                String url2 = "https://www.dictionaryapi.com/api/v3/references/medical/json/" + input + "?key=4dcf350b-7351-4f26-ba8a-ffe0824b415d";
                URL obj = new URL(url);
                URL obj2 = new URL(url2);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
                String inputLine;
                String inputLine2;
                StringBuffer response = new StringBuffer();
                StringBuffer response2 = new StringBuffer();
                while ((inputLine = in.readLine()) != null){
                    response.append(inputLine);
                }

                while ((inputLine2 = in2.readLine()) != null){
                    response2.append(inputLine2);
                }

                in.close();
                if (usedWords.contains(input)){
                    channel.sendMessage("You entered a used word again. You're eliminated").queue();
                    timeForResponse = 10000;
                    players.remove(e.getAuthor().getIdLong());
                    turnsElapsed--;
                    game(ctx, channel);
                }


                try {
                    if (response.toString().startsWith("meta", 3) || response2.toString().startsWith("meta", 3)){
                        if (input.contains(prompt) || input.contains(prompt.toLowerCase())){
                            channel.sendMessage("Success. That word exists").queue();
                            usedWords.add(input);
                            if (turnsElapsed % players.size() == players.size() - 1){
                                timeForResponse -= 500;
                            }
                            turnsElapsed++;
                        } else {
                            channel.sendMessage("Your word doesn't contain the prompt. You are eliminated").queue();
                            timeForResponse = 10000;
                            turnsElapsed--;
                            shootThreeWords = false;
                            players.remove(e.getAuthor().getIdLong());
                        }
                    } else {
                        channel.sendMessage("That word doesn't exist. You are eliminated").queue();
                        players.remove(e.getAuthor().getIdLong());
                        timeForResponse = 10000;
                        turnsElapsed--;
                        shootThreeWords = false;
                    }
                } catch (Exception pp){
                    channel.sendMessage("That word doesn't exist. You are eliminated").queue();
                    players.remove(e.getAuthor().getIdLong());
                    timeForResponse = 10000;
                    turnsElapsed--;
                    shootThreeWords = false;
                }
                game(ctx,channel);

            } catch (IOException pp){
                pp.printStackTrace();
            }
        }, timeForResponse, TimeUnit.MILLISECONDS, () -> {
            channel.sendMessage("You didn't respond in time. You're out").queue();
            players.remove(event.getAuthor().getIdLong());
            timeForResponse = 10000;
            shootThreeWords = false;
            game(ctx, channel);
        });
    }

    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        starting(ctx, channel);
    }

    @Override
    public String getName() {
        return "wordbomb";
    }

    @Override
    public String getHelp() {
        return "Play wordbomb with other players";
    }
}
