package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.standardcommand.gamesSetupFiles.Node;
import jda.standardcommand.music.ResumeCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class StrategicTicTacToe implements ICommand {
    private final EventWaiter waiter;
    public StrategicTicTacToe(EventWaiter waiter) {
        this.waiter = waiter;
    }
    Random rand = new Random();
    boolean startFirst = false;
    String[][] board = new String[3][3];
    String boardAsString = ":white_large_square::white_large_square::white_large_square:\n:white_large_square::white_large_square:" +
            ":white_large_square:\n:white_large_square::white_large_square::white_large_square:";
    ArrayList<Integer> buttonsAlreadyClicked = new ArrayList<Integer>();

    //

    //


    /**
     * 1 represents player winning, -1 represents computer winning, 0 represents nobody winning
     * @return
     */
    public int checkWinLose(){

        //check rows
        for (int i = 0; i < 3; i++){
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])){
                if (board[i][0].equals(":x:")){
                    return 1;
                } else if (board[i][0].equals(":o:")){
                    return -1;
                }
            }
        }

        //check columns
        for (int i = 0; i < 3; i++){
            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])){
                if (board[0][i].equals(":x:")){
                    return 1;
                } else if (board[0][i].equals(":o:")){
                    return -1;
                }
            }
        }

        //check negative slope diagonal
        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])){
            if (board[0][0].equals(":x:")){
                return 1;
            } else if (board[0][0].equals(":o:")){
                return -1;
            }
        }

        //check positive slope diagonal
        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])){
            if (board[0][2].equals(":x:")){
                return 1;
            } else if (board[0][2].equals(":o:")){
                return -1;
            }
        }

        return 0;
    }

    public void starting(CommandContext ctx, TextChannel channel){
        channel.sendMessage("Welcome to TicTacToe. The rules are simple. " +
                "Click the button indicating the tile you want to cover. If you" +
                "want to quit the game, make sure you press STOP, otherwise there" +
                "will be problems. Alright, Lets play! Do you want to go first? Reply with yes or no").queue((message) -> {

            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        for (int i = 0; i < 3; i++){
                            Arrays.fill(board[i], ":white_large_square:");
                            updateBoard();
                        }
                        buttonsAlreadyClicked.clear();
                        if (e.getMessage().getContentRaw().equalsIgnoreCase("Yes")){
                            channel.sendMessage("You chose to start first. Let's get started").queue();
                            startFirst = true;
                        } else if (e.getMessage().getContentRaw().equalsIgnoreCase("no")){
                            channel.sendMessage("You chose not to start first. Let's get started").queue();
                            //pick a corner spot
                            int[] possibleFirstMoves = {1,3,7,9};
                            int choose = rand.nextInt(4);
                            int randomFirstSquare = possibleFirstMoves[choose] - 1;
                            int computerXCoordinate = randomFirstSquare / 3;
                            int computerYCoordinate = randomFirstSquare % 3;
                            board[computerXCoordinate][computerYCoordinate] = ":o:";
                            buttonsAlreadyClicked.add(randomFirstSquare);
                            updateBoard();

                        } else {
                            channel.sendMessage("That is not a valid response. Please try this command again.").queue();
                            return;
                        }
                        game(ctx, channel);

                    },
                    5, TimeUnit.SECONDS,
                    () -> channel.sendMessage("You didn't respond in time").queue()
            );
        });
    }

    public void game(CommandContext ctx, TextChannel channel){
        channel.sendMessage(boardAsString).
                setActionRows(ActionRow.of(Button.secondary("1", "1"), Button.secondary("2", "2"), Button.secondary("3", "3"),
                        Button.secondary("4", "4"), Button.secondary("5", "5")), ActionRow.of(Button.secondary("6", "6"),
                        Button.secondary("7", "7"), Button.secondary("8", "8"),
                        Button.secondary("9", "9"), Button.secondary("STOP", "STOP"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        channel.sendMessage("------------------------------------------------------").queue();
                        int buttonClicked = 0;
                        try {
                            buttonClicked = Integer.parseInt(e.getButton().getId()) - 1;
                        } catch (NumberFormatException pp){
                            channel.sendMessage("You quit your game! Mad cuz bad").queue();
                            return;
                        }

                        if (buttonsAlreadyClicked.contains(buttonClicked)){
                            channel.sendMessage("You already clicked that tile. For now i'm terminating the program. In the future I'll allow you to retry don't worry").queue();
                            return;
                        }
                        buttonsAlreadyClicked.add(buttonClicked);


                        //player move
                        int xCoordinate = buttonClicked / 3;
                        int yCoordinate = buttonClicked % 3;
                        board[xCoordinate][yCoordinate] = ":x:";
                        updateBoard();
                        channel.sendMessage(boardAsString).queue();
                        if (checkWinLose() == 1){
                            channel.sendMessage("You Won! ***Sheeesh***, you're simply built different from the rest my guy").queue();
                            return;
                        }

                        if (buttonsAlreadyClicked.size() == 9){
                            channel.sendMessage("Tie").queue();
                            return;
                        }

                        //computer generated move
                        generateOptimalMove();

                        /*
                        while(true){
                            int randomNumber = rand.nextInt(9);
                            if (!buttonsAlreadyClicked.contains(randomNumber)){
                                int computerXCoordinate = randomNumber / 3;
                                int computerYCoordinate = randomNumber % 3;
                                board[computerXCoordinate][computerYCoordinate] = ":o:";
                                buttonsAlreadyClicked.add(randomNumber);
                                updateBoard();
                                break;
                            }
                        }
                         */

                        if (checkWinLose() == -1){
                            channel.sendMessage(boardAsString).queue();
                            channel.sendMessage("The bot won. You lost. How can you anybody be this bad").queue();
                            return;
                        } else if (buttonsAlreadyClicked.size() == 9){
                            channel.sendMessage(boardAsString).queue();
                            channel.sendMessage("Tie").queue();
                            return;
                        }



                        game(ctx, channel);
                    },
                    20, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Cmon you had 20 seconds to make a move, you slow duck").queue()
            );
        });
    }

    public void updateBoard(){
        boardAsString = "";
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                boardAsString += board[i][j];
            }
            boardAsString += "\n";
        }

    }

    public void generateOptimalMove(){
        if (buttonsAlreadyClicked.size() == 1){
            //if player chooses corner move, computer does center, if player chooses center, computer does corner, if player choose edge, computer does opposite edge
        } else if (buttonsAlreadyClicked.size() == 2){
            //if player chooses edge square, computer does center or corner and wins (depends, figure this out later). if player chooses center, go opposite corner from first corner
        } // so on
    }



    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        starting(ctx, channel);


    }

    @Override
    public String getName() {
        return "impossibletictactoe";
    }

    @Override
    public String getHelp() {
        return "play tictactoe with the bot, but the bot actually uses its brain";
    }
}
