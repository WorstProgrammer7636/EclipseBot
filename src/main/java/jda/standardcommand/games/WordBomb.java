package jda.standardcommand;

import com.fasterxml.jackson.databind.JsonNode;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WordBomb extends ListenerAdapter implements ICommand  {
    private final EventWaiter waiter;
    ArrayList<Long> players = new ArrayList<Long>();
    List<User> invitedPlayers;
    ArrayList<String> actualInvitedPlayers = new ArrayList<String>();
    int turnsElapsed = 0;

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
                            //public
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

    public void publicStart(CommandContext ctx, TextChannel channel){

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

    public void game(CommandContext ctx, TextChannel channel) {
        channel.sendMessage("Enter word please").queue();
        this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> {
            long nchannel = e.getChannel().getIdLong();
            long nuser = e.getMember().getUser().getIdLong();
            return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
        }, e -> {
            String input = e.getMessage().getContentRaw();
                    try {
                        String url = "https://www.dictionaryapi.com/api/v3/references/thesaurus/json/" + input + "?key=b0f8a31e-659b-420e-b912-16d723b56ff8";
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                        int responseCode = con.getResponseCode();
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null){
                            response.append(inputLine);
                        }
                        in.close();

                        if (response.toString().equals("[]")){
                            channel.sendMessage("That is not a valid word").queue();
                            return;
                        } else {
                            channel.sendMessage("Success. That word exists").queue();
                        }
                        System.out.println(response.toString());
                    } catch (IOException pp){
                        pp.printStackTrace();
                    }
        },
                30, TimeUnit.SECONDS, () -> channel.sendMessage("Too slow").queue());




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
        return "play word bomb with other players";
    }
}
