package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.standardcommand.gamesSetupFiles.MonopolyProperties;
import jda.standardcommand.gamesSetupFiles.RailRoad;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Monopoly implements ICommand {
    private final EventWaiter waiter;
    ArrayList<Long> players = new ArrayList<Long>();
    ArrayList<String> usedWords = new ArrayList<String>();
    List<User> invitedPlayers;
    ArrayList<String> actualInvitedPlayers = new ArrayList<String>();
    HashMap<Integer, MonopolyProperties> board = new HashMap<>();


    Random rand = new Random();
    public Monopoly(EventWaiter waiter){
        this.waiter = waiter;
    }

    public void initiateBoard(){
        //name, color, price, mortgageValue, rent, rentWithColor, oneHouse, twoHouse, threeHouse, fourHouse, hotel


        MonopolyProperties GO = new MonopolyProperties("go", "none", -200, 0, 0, 0, 0, 0,0, 0, 0, 0,false);
        MonopolyProperties MEDITERRANEAN = new MonopolyProperties("mediterranean", "brown", 60, 30, 33, 2, 4, 10, 30, 90, 160,250, false);
        MonopolyProperties COMMUNITYCHEST1 = new MonopolyProperties("communitychest1", "none", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        MonopolyProperties BALTIC = new MonopolyProperties("baltic", "brown", 60, 30,33,4,8,20,60,180,320,450,false);
        MonopolyProperties INCOMETAX = new MonopolyProperties("incometax", "none", 200,0,0,0,0,0,0,0,0,0,false);
        //MonopolyProperties READINGRAILROAD = new MonopolyProperties("readingrailroad", "railroad", 200,100,110, 25, 0, 50,;
        //MonopolyProperties ORIENTAL = new MonopolyProperties("oriental", "sky", 100);
        //MonopolyProperties CHANCE1 = new MonopolyProperties("chance1", "none", 0);
        //MonopolyProperties VERMONT = new MonopolyProperties("vermont", "sky", 100);
        //MonopolyProperties CONNECTICUT = new MonopolyProperties("connecticut", "sky", 120);
        //MonopolyProperties JAIL = new MonopolyProperties("jail", "none", 50, 0, 0, 0, 0, 0, 0, false);



        for (int i = 0; i < MonopolyProperties.properties.size(); i++){
            board.put(i, MonopolyProperties.properties.get(i));
        }

        System.out.println(board.get(0).getMortgageValue());
    }



    public void starting(CommandContext ctx, TextChannel channel){
        initiateBoard();
        channel.sendMessage("You started a game of Monopoly. Ping everyone you want to invite in one message. Maximum 5 players including yourself").queue((message) -> {
            this.waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> {
                long nchannel = e.getChannel().getIdLong();
                long nuser = e.getMember().getUser().getIdLong();
                return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
            }, e -> {
                players.clear();
                players.add(e.getAuthor().getIdLong());
                actualInvitedPlayers.clear();
                invitedPlayers = e.getMessage().getMentionedUsers();
                for (User invitedPlayer : invitedPlayers) {
                    actualInvitedPlayers.add(String.valueOf(invitedPlayer));
                }
                if (invitedPlayers.contains(e.getAuthor())){
                    channel.sendMessage("Hey, you're already in the game. You can't invite yourself!").queue();
                    return;
                }

                if (invitedPlayers.size() > 5){
                    channel.sendMessage("you invited more than 4 people. please try again").queue();
                    return;
                }

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
        if (players.size() == 1){
            channel.sendMessage("The game has ended").queue();
            return;
        }



    }


    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        starting(ctx, channel);
    }

    @Override
    public String getName() {
        return "monopoly";
    }

    @Override
    public String getHelp() {
        return "Play monopoly with other players";
    }
}
