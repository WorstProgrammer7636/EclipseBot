package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MultiplayerConnectFour implements ICommand {
    private final EventWaiter waiter;

    public MultiplayerConnectFour(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        String[][] board = new String[6][7];
        ArrayList<Long> players = new ArrayList<Long>();
        if (ctx.getArgs().isEmpty()){
            ctx.getChannel().sendMessage("You need to mention someone to play with! Example: ?connectfour @player123").queue();
            return;
        } else if (ctx.getArgs().size() > 1){
            ctx.getChannel().sendMessage("You can only mention ONE person to play with!. Example: ?connectfour @player123").queue();
            return;
        }

        long playerID;
        long opponentID;
        try {
             playerID = ctx.getAuthor().getIdLong();
             opponentID = ctx.getMessage().getMentionedMembers().get(0).getIdLong();

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
            ctx.getChannel().sendMessage("Please mention a member to play with! Example: ?connectfour @player123").queue();
            return;
        }

        /**
         * Pass all message checks, get who goes first
         */
        getWhoGoesFirst(ctx, board, players, playerID, opponentID);

    }

    public void getWhoGoesFirst(CommandContext ctx, String[][] board, ArrayList<Long> players, long playerID, long opponentID){
        ctx.getChannel().sendMessage("<@" + ctx.getAuthor().getIdLong() + ">. Please choose who goes first.").
                setActionRow(Button.success("player", ctx.getAuthor().getName()),
                        Button.danger("opponent", ctx.getMessage().getMentionedMembers().get(0).getEffectiveName())).queue((message -> {
            this.waiter.waitForEvent(ButtonClickEvent.class, (e) -> {
                long nchannel = e.getChannel().getIdLong();
                long nuser = e.getMember().getUser().getIdLong();
                return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
            }, (e) -> {
                boolean isPlayersTurn = false;
                boolean isOpponentsTurn = false;
                if (e.getButton().getId().equals("player")){
                    isPlayersTurn = true;
                } else {
                    isOpponentsTurn = true;
                }

                /**
                 * Set up board, then
                 * Start Game after bot receives who goes first
                 */
                setupBoard(ctx, board, isPlayersTurn, isOpponentsTurn, playerID, opponentID);
                startGame(ctx, board, players, isPlayersTurn, isOpponentsTurn, playerID, opponentID);
            }, 90, TimeUnit.SECONDS, () -> {
                ctx.getChannel().sendMessage("You took too long to respond. Run this command again").queue();
            });
        }));
    }

    public void setupBoard(CommandContext ctx, String[][] board, boolean isPlayersTurn, boolean isOpponentsTurn, long playerID, long opponentID){
        String firstPersonToGo = "";
        if (isPlayersTurn){
            firstPersonToGo = "<@" + playerID + ">";
        } else if (isOpponentsTurn){
            firstPersonToGo = "<@" + opponentID + ">";
        }
        long boardId = ctx.getAuthor().getIdLong();
        StringBuilder boardAsString = new StringBuilder();
        for (int i = 0; i < 6; i++){
            Arrays.fill(board[i], ":white_circle:");
        }

        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 7; j++){
                boardAsString.append(board[i][j]);
            }
            boardAsString.append("\n");
        }

        ctx.getChannel().sendMessage(firstPersonToGo + " ***YOUR TURN***\n" + boardAsString).setActionRows(
                ActionRow.of(Button.primary("1" + boardId, "1"),
                        Button.primary("2" + boardId, "2"),
                        Button.primary("3" + boardId, "3"),
                        Button.primary("4" + boardId, "4")),
                ActionRow.of(Button.primary("5" + boardId, "5"),
                        Button.primary("6" + boardId, "6"),
                        Button.primary("7" + boardId, "7"),
                        Button.primary("QUIT" + boardId, "QUIT"))
        ).queue();
    }

    public void startGame(CommandContext ctx, String[][] board, ArrayList<Long> players, boolean isPlayersTurn, boolean isOpponentsTurn, long playerID, long opponentID){
        this.waiter.waitForEvent(ButtonClickEvent.class, (e) -> {
            long nchannel = e.getChannel().getIdLong();
            long nuser = e.getMember().getUser().getIdLong();
            boolean correctPersonClickedButton = false;
            if (isPlayersTurn && nuser == playerID){
                correctPersonClickedButton = true;
            } else if (isOpponentsTurn && nuser == opponentID){
                correctPersonClickedButton = true;
            }
            return ctx.getChannel().getIdLong() == nchannel && players.contains(nuser) && correctPersonClickedButton;
        }, (e) -> {
            //check that it is the turn of the player who clicked the button, otherwise deny input
            boolean playerTurn = isPlayersTurn;
            boolean opponentsTurn = isOpponentsTurn;


            //game
            if (e.getButton().getId().equals("QUIT" + ctx.getAuthor().getIdLong())){
                ctx.getChannel().sendMessage("You forfeit the game.").queue();
                return;
            }

            int column = 0;
            try {
                column = Integer.parseInt(String.valueOf(e.getButton().getId().charAt(0)));
                String[][] updatedBoard = dropToken(ctx, e, board, column, playerTurn, opponentsTurn);
                e.editMessage(editBoard(updatedBoard)).queue();
                if (checkForWin(updatedBoard)){
                    if (isPlayersTurn){
                        ctx.getChannel().sendMessage("CONGRATULATIONS <@" + playerID + ">. You won!!!! :partying_face: :partying_face:").queue();
                        return;
                    } else if (isOpponentsTurn){
                        ctx.getChannel().sendMessage("CONGRATULATIONS <@" + opponentID + ">. You won!!!! :partying_face: :partying_face:").queue();
                        return;
                    }
                }

                //swap turn
                playerTurn = !playerTurn;
                opponentsTurn = !opponentsTurn;

                //next turn
                startGame(ctx, updatedBoard, players, playerTurn, opponentsTurn, playerID, opponentID);
            } catch (NumberFormatException numberFormatException){
                ctx.getChannel().sendMessage("Please only click the buttons on your board right now").queue();
                startGame(ctx, board, players, playerTurn, opponentsTurn, playerID, opponentID);
            }

        }, 90, TimeUnit.SECONDS, () -> {
            ctx.getChannel().sendMessage("You took too long to respond. Run this command again").queue();
        });
    }

    public String[][] dropToken(CommandContext ctx, ButtonClickEvent e, String[][] board, int column, boolean playerTurn, boolean opponentsTurn){
        String color = "";
        if (playerTurn){
            color = ":red_circle:";
        } else {
            color = ":yellow_circle:";
        }
        boolean columnIsFull = true;
        for (int i = 5; i >= 0; i--){
            if (board[i][column - 1].equals(":white_circle:")){
                board[i][column - 1] = color;
                columnIsFull = false;
                break;
            }
        }

        if (columnIsFull){
            ctx.getChannel().sendMessage("This column is full!").queue();
        }

        return board;
    }

    public String editBoard(String[][] board){
        String editedBoard = "";
        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 7; j++){
                editedBoard += board[i][j];
            }
            editedBoard += "\n";
        }

        return editedBoard;
    }

    public boolean checkForWin(String[][] board){
        if (horizontalWinPresent(board)){
            return true;
        } else if (verticalWinPresent(board)){
            return true;
        } else if (LeftDiagonalWinPresent(board)){
            return true;
        } else if (rightDiagonalWinPresent(board)){
            return true;
        } else {
            return false;
        }
    }

    public boolean horizontalWinPresent(String[][] board){
        boolean isWon = false;
        try {
            for (int i = 0; i < 6; i++){
                for (int j = 0; j < 4; j++){
                    if (!board[i][j].equals(":white_circle:") && board[i][j].equals(board[i][j+1]) && board[i][j+1].equals(board[i][j+2]) && board[i][j+2].equals(board[i][j+3])){
                        isWon = true;
                        break;
                    }
                }

                if (isWon){
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
            System.out.println("bound exception. shouldn't happen");
        }

        return isWon;
    }

    public boolean verticalWinPresent(String[][] board){
        boolean isWon = false;
        try {
            for (int i = 0; i < 3; i++){
                for (int j = 0; j < 7; j++){
                    if (!board[i][j].equals(":white_circle:") && board[i][j].equals(board[i+1][j]) && board[i+1][j].equals(board[i+2][j]) && board[i+2][j].equals(board[i+3][j])){
                        isWon = true;
                        break;
                    }
                }

                if (isWon){
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
            System.out.println("bound exceptino. shouldn't happen");
        }

        return isWon;
    }

    public boolean LeftDiagonalWinPresent(String[][] board){
        boolean isWon = false;
        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 7; j++){
                try {
                    if (!board[i][j].equals(":white_circle:") && board[i][j].equals(board[i+1][j+1]) && board[i+1][j+1].equals(board[i+2][j+2]) && board[i+2][j+2].equals(board[i+3][j+3])){
                        isWon = true;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
                    continue;
                }
            }

            if (isWon){
                break;
            }
        }

        return isWon;
    }

    public boolean rightDiagonalWinPresent(String[][] board){
        boolean isWon = false;
        for (int i = 0; i < 6; i++){
            for (int j = 0; j < 7; j++){
                try {
                    if (!board[i][j].equals(":white_circle:") && board[i][j].equals(board[i-1][j+1]) && board[i-1][j+1].equals(board[i-2][j+2]) && board[i-2][j+2].equals(board[i-3][j+3])){
                        isWon = true;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
                    continue;
                }

                if (isWon){
                    break;
                }
            }
        }

        return isWon;
    }

    @Override
    public String getName() {
        return "connectfour";
    }

    @Override
    public String getHelp() {
        return "play connectfour with another person";
    }
}
