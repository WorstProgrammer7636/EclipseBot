package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Battleship implements ICommand {
    private final EventWaiter waiter;

    public Battleship(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        ArrayList<Long> players = new ArrayList<Long>();
        if (ctx.getArgs().isEmpty()){
            ctx.getChannel().sendMessage("You need to mention someone to play with! Example: ?battleship @player123").queue();
            return;
        } else if (ctx.getArgs().size() > 1){
            ctx.getChannel().sendMessage("You can only mention ONE person to play with!. Example: ?battleship @player123").queue();
            return;
        }

        long playerID;
        long opponentID;
        User playerUser;
        User opponentUser;
        try {
            playerID = ctx.getAuthor().getIdLong();
            opponentID = ctx.getMessage().getMentionedMembers().get(0).getIdLong();
            playerUser = ctx.getAuthor();
            opponentUser = ctx.getMessage().getMentionedMembers().get(0).getUser();
            if (opponentID == ctx.getAuthor().getIdLong()){
                ctx.getChannel().sendMessage("You can't play by yourself bozo.").queue();
                return;
            } else if (opponentID == Long.parseLong("1003478249755656273")){
                ctx.getChannel().sendMessage("Sorry! You can't play with me! Find some friends lol").queue();
                return;
            }

            players.add(playerID);
            players.add(opponentID);
        } catch (Exception exception) {
            ctx.getChannel().sendMessage("Please mention a member to play with! Example: ?battleship @player123").queue();
            return;
        }

        getOpponentsPermission(ctx, playerID, opponentID, playerUser, opponentUser);
    }

    public void getOpponentsPermission(CommandContext ctx, Long playerID, Long opponentID, User playerUser, User opponentUser){
        ctx.getChannel().sendMessage("<@" + opponentID + ">. You were invited to play a round of Battleship with <@" + playerID + ">. Do you accept " +
                "the invitation?").
                setActionRow(Button.success("player", ctx.getAuthor().getName()),
                        Button.danger("opponent", ctx.getMessage().getMentionedMembers().get(0).getEffectiveName())).queue((message -> {
            this.waiter.waitForEvent(ButtonClickEvent.class, (e) -> {
                long nchannel = e.getChannel().getIdLong();
                long nuser = e.getMember().getUser().getIdLong();
                return ctx.getChannel().getIdLong() == nchannel && nuser == opponentID;
            }, (e) -> {
                boolean opponentAccepted = false;
                if (e.getButton().getId().equalsIgnoreCase("accept")){
                    opponentAccepted = true;
                } else {
                    ctx.getChannel().sendMessage("Invitation declined!").queue();
                    return;
                }

                if (opponentAccepted){
                    DMUsersToSetUpBoard(ctx, playerID, opponentID, playerUser, opponentUser);
                }
            }, 25, TimeUnit.SECONDS, () -> {
                ctx.getChannel().sendMessage("Invitation declined due to timeout!").queue();
            });
        }));
    }

    public void DMUsersToSetUpBoard(CommandContext ctx, Long playerID, Long opponentID, User playerUser, User opponentUser){
        try {
            playerUser.openPrivateChannel().queue((DM) ->
            {
                DM.sendMessage("Hello there!").queue(message -> {
                    this.waiter.waitForEvent(
                            PrivateMessageReceivedEvent.class,
                            (e) -> {
                                long nchannel = e.getChannel().getIdLong();
                                return DM.getIdLong() == nchannel && !e.getAuthor().isBot();
                            },
                            (e) -> {

                            },
                            140, TimeUnit.SECONDS,
                            () -> {
                                ctx.getChannel().sendMessage("You took too long to respond. Please try this command again").queue();
                            }
                    );
                });

            });
        } catch (Exception exception){
            ctx.getChannel().sendMessage("Bot was unable to DM <@" + playerID + ">. Please make sure your DMs are on and available for the bot").queue();
        }

        try {
            opponentUser.openPrivateChannel().queue((DM) ->
            {
                DM.sendMessage("Hello there!").queue(message -> {
                    this.waiter.waitForEvent(
                            PrivateMessageReceivedEvent.class,
                            (e) -> {
                                long nchannel = e.getChannel().getIdLong();
                                return DM.getIdLong() == nchannel && !e.getAuthor().isBot();
                            },
                            (e) -> {

                            },
                            140, TimeUnit.SECONDS,
                            () -> {
                                ctx.getChannel().sendMessage("You took too long to respond. Please try this command again").queue();
                            }
                    );
                });

            });
        } catch (Exception exception){
            ctx.getChannel().sendMessage("Bot was unable to DM <@" + playerID + ">. Please make sure your DMs are on and available for the bot").queue();
        }
    }

    @Override
    public String getName() {
        return "battleship";
    }

    @Override
    public String getHelp() {
        return "play battleship with someone";
    }
}
