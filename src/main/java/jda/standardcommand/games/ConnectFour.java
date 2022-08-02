package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ConnectFour implements ICommand {
    private final EventWaiter waiter;
    public ConnectFour(EventWaiter waiter){
        this.waiter = waiter;
    }

    Random r = new Random();
    String[][] board = new String[6][7];
    int[] piecesInColumns = new int[8];
    String boardAsString = "";
    int movesMade = 0;


    public void updateBoard(){
        boardAsString = "";
        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 7; j++){
                boardAsString += board[i][j];
            }
            boardAsString += "\n";
        }
    }

    public boolean boardIsFull(){
        if (movesMade >= 42){
            return true;
        }
        return false;
    }

    /**
     * 16 checks per piece placed
     * @param x
     * @param y
     * @param color
     * @return
     */
    public int checkWinLose(int x, int y, String color){
        ArrayList<Integer> lines = new ArrayList<Integer>();
        int h = checkHorizontal(x, y, color);
        int v = checkVertical(x, y, color);
        int d1 = checkAscendingDiagonal(x, y, color);
        int d2 = checkDescendingDiagonal(x, y, color);

        lines.add(h);
        lines.add(v);
        lines.add(d1);
        lines.add(d2);
        if (lines.contains(1) && lines.contains(-1)){
            //note to self that if this condition ever becomes true there is a very big fucking problem
            System.exit(1);
        }
        if (lines.contains(1)){
            return 1;
        } else if (lines.contains(-1)){
            return -1;
        }


        return 0;
    }




    public void starting(CommandContext ctx, TextChannel channel){
        channel.sendMessage("Welcome to Connect Four. The rules are simple. " +
                "Click the button indicating the column you want to drop a piece in. Note that you are always the red pieces. " +
                "If you" +
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
                        for (int i = 0; i < 6; i++){
                            Arrays.fill(board[i], ":white_circle:");
                        }
                        Arrays.fill(piecesInColumns, 5);
                        movesMade = 0;
                        updateBoard();

                        if (e.getMessage().getContentRaw().equalsIgnoreCase("yes")){
                            channel.sendMessage("Great. Let's get started").queue();
                        } else if (e.getMessage().getContentRaw().equalsIgnoreCase("no")){
                            channel.sendMessage("Great. Let's get started").queue();
                            //bot makes a random move

                            movesMade++;
                        } else {
                            channel.sendMessage("That is not a valid response. Please run this command again.").queue();
                            return;
                        }

                        game(ctx, channel);


                    },
                    10, TimeUnit.SECONDS,
                    () -> channel.sendMessage("You didn't respond in time").queue()
            );
        });
    }

    public void game(CommandContext ctx, TextChannel channel){
        channel.sendMessage(boardAsString).
                setActionRows(ActionRow.of(Button.secondary("1", "1"), Button.secondary("2", "2"), Button.secondary("3", "3"),
                        Button.secondary("4", "4"), Button.secondary("5", "5")), ActionRow.of(Button.secondary("6", "6"),
                        Button.secondary("7", "7"), Button.secondary("STOP", "STOP"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        channel.sendMessage("-------------------------------").queue();
                        int buttonClicked = 0;
                        try {
                            buttonClicked = Integer.parseInt(e.getButton().getId()) - 1;
                        } catch (NumberFormatException pp){
                            channel.sendMessage("You quit your game! Mad cuz bad").queue();
                            return;
                        }


                        //player move
                        if (piecesInColumns[buttonClicked] < 0){
                            channel.sendMessage("You can't drop there anymore cuz it's full. Game ended").queue();
                            return;
                        }

                        int playerXMove = piecesInColumns[buttonClicked];
                        board[piecesInColumns[buttonClicked]][buttonClicked] = ":red_circle:";
                        updateBoard();
                        piecesInColumns[buttonClicked]--;
                        movesMade++;
                        channel.sendMessage(boardAsString).queue();

                        if (checkWinLose(playerXMove, buttonClicked, ":red_circle:") == 1){
                            channel.sendMessage("You won! ***Sheeeesh***, you're just built different from the rest").queue();
                            return;
                        }

                        if (boardIsFull()){
                            channel.sendMessage("Tie. ").queue();
                            return;
                        }

                        //computer generated move
                        int computerXMove = 0;
                        int computerYMove = 0;
                        while (true){
                            int randomColumn = r.nextInt(7);
                            if (piecesInColumns[randomColumn] >= 0){
                                board[piecesInColumns[randomColumn]][randomColumn] = ":yellow_circle:";
                                updateBoard();
                                computerXMove = piecesInColumns[randomColumn];
                                computerYMove = randomColumn;
                                piecesInColumns[randomColumn]--;
                                movesMade++;

                                break;
                            }
                        }

                        if (checkWinLose(computerXMove, computerYMove, ":yellow_circle:") == -1){
                            channel.sendMessage(boardAsString).queue();
                            channel.sendMessage("You lost. Damn you are trash").queue();
                            return;
                        } else if (boardIsFull()){
                            channel.sendMessage(boardAsString).queue();
                            channel.sendMessage("Tie. Game ended").queue();
                            return;
                        }


                        game(ctx, channel);


                    },
                    60, TimeUnit.SECONDS,
                    () -> channel.sendMessage("Cmon you had 60 seconds to make a move, you slow duck").queue()
            );
        });
    }

    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        starting(ctx, channel);
    }

    @Override
    public String getName() {
        return "connectfour";
    }

    @Override
    public String getHelp() {
        return "Play ConnectFour with the bot. Usage: ?connectfour";
    }

    public int checkHorizontal(int x, int y, String color){
        //horizontal right
        try {
            if (board[x][y].equals(color) && board[x][y + 1].equals(color) && board[x][y + 2].equals(color) && board[x][y + 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //nothing happens
        }

        //horizontal left
        try {
            if (board[x][y].equals(color) && board[x][y - 1].equals(color) && board[x][y - 2].equals(color) && board[x][y - 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //nothing happens
        }

        //horizontal in between
        try {
            if (board[x][y - 1].equals(color) && board[x][y].equals(color) && board[x][y + 1].equals(color) && board[x][y+2].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        //horizontal in between
        try {
            if (board[x][y - 2].equals(color) && board[x][y - 1].equals(color) && board[x][y].equals(color) && board[x][y+1].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        return 0;
    }

    public int checkVertical(int x, int y, String color){
        //horizontal right
        try {
            if (board[x][y].equals(color) && board[x + 1][y].equals(color) && board[x + 2][y].equals(color) && board[x + 3][y].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //nothing happens
        }

        //horizontal left
        try {
            if (board[x][y].equals(color) && board[x - 1][y].equals(color) && board[x - 2][y].equals(color) && board[x - 3][y].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //nothing happens
        }

        //horizontal in between
        try {
            if (board[x - 1][y].equals(color) && board[x][y].equals(color) && board[x + 1][y].equals(color) && board[x + 2][y].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        //horizontal in between
        try {
            if (board[x - 2][y].equals(color) && board[x - 1][y].equals(color) && board[x][y].equals(color) && board[x + 1][y].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }
        return 0;
    }

    public int checkAscendingDiagonal(int x, int y, String color){
        try {
            if (board[x][y].equals(color) && board[x - 1][y + 1].equals(color) && board[x - 2][y + 2].equals(color) && board[x - 3][y + 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        try {
            if (board[x][y].equals(color) && board[x + 1][y - 1].equals(color) && board[x + 2][y - 2].equals(color) && board[x + 3][y - 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        try {
            if (board[x + 1][y - 1].equals(color) && board[x][y].equals(color) && board[x - 1][y + 1].equals(color) && board[x - 2][y + 2].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        try {
            if (board[x + 2][y - 2].equals(color) && board[x + 1][y - 1].equals(color) && board[x][y].equals(color) && board[x - 1][y + 1].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        return 0;
    }

    public int checkDescendingDiagonal(int x, int y, String color){
        try {
            if (board[x][y].equals(color) && board[x - 1][y - 1].equals(color) && board[x - 2][y - 2].equals(color) && board[x - 3][y - 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }
        try {
            if (board[x][y].equals(color) && board[x + 1][y + 1].equals(color) && board[x + 2][y + 2].equals(color) && board[x + 3][y + 3].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        try {
            if (board[x + 1][y + 1].equals(color) && board[x][y].equals(color) && board[x - 1][y - 1].equals(color) && board[x - 2][y - 2].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        try {
            if (board[x + 2][y + 2].equals(color) && board[x + 1][y + 1].equals(color) && board[x][y].equals(color) && board[x - 1][y - 1].equals(color)){
                if (color.equals(":red_circle:")){
                    return 1;
                } else if (color.equals(":yellow_circle:")){
                    return -1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            //do nothing
        }

        return 0;
    }
}
