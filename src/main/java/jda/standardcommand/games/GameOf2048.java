package jda.standardcommand.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.standardcommand.gamesSetupFiles.TwentyFourtyEightTiles;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameOf2048 implements ICommand {
    private final EventWaiter waiter;
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        //initialize
        List<Emote> emotes = ctx.getGuild().getEmotes();
        HashMap<String, Long> emoteInfo = new HashMap<>();
        TwentyFourtyEightTiles tile = new TwentyFourtyEightTiles();
        for (Emote emote : emotes) {
            emoteInfo.put(emote.getName(), emote.getIdLong());
        }

        if (!allEmotesInstalled(ctx, emoteInfo)){
            return;
        }
        int score = 0;
        String[][] board = new String[4][4];
        setUpSettings(ctx, board, tile.blankTile, tile, emoteInfo);
        generateTwoRandomTiles(ctx, board, tile.blankTile, tile, emoteInfo);
        displayBoard(ctx, board, emoteInfo);
        startGame(ctx, board, tile, emoteInfo, score);
    }

    public String displayEmotes(HashMap<String, Long> emoteInfo, String emoteToGet){
        return "<:" + emoteToGet + ":" + emoteInfo.get(emoteToGet) + ">";
    }

    public void setUpSettings(CommandContext ctx, String[][] board, String blankTile, TwentyFourtyEightTiles tile,
                              HashMap<String, Long> emoteInfo){
        Arrays.fill(board[0], blankTile);
        Arrays.fill(board[1], blankTile);
        Arrays.fill(board[2], blankTile);
        Arrays.fill(board[3], blankTile);
    }

    public void generateTwoRandomTiles(CommandContext ctx, String[][] board, String blankTile, TwentyFourtyEightTiles tile,
                                       HashMap<String, Long> emoteInfo){
        Random randomGen = new Random();
        String fourTile = displayEmotes(emoteInfo, "4Tile");
        String twoTile = displayEmotes(emoteInfo, "2Tile");

        //generate first random tile at start
        int firstX = randomGen.nextInt(4);
        int secondX = randomGen.nextInt(4);

        //generate second random tile at start
        int thirdX = randomGen.nextInt(4);
        int fourthX = randomGen.nextInt(4);

        //regenerate second random tile in the case of duplicate tile generation
        while (firstX == thirdX && secondX == fourthX){
            thirdX = randomGen.nextInt(4);
            fourthX = randomGen.nextInt(4);
        }

        //10% chance of first random tile being a 4
        boolean firstTileIsFour = randomGen.nextInt(10) == 3;
        boolean secondTileIsFour = randomGen.nextInt(10) == 8;
        if (firstTileIsFour){
            board[firstX][secondX] = fourTile;
        } else {
            board[firstX][secondX] = twoTile;
        }

        if (secondTileIsFour){
            board[thirdX][fourthX] = fourTile;
        } else {
            board[thirdX][fourthX] = twoTile;
        }

    }

    public void generateARandomTile(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo){
        ArrayList<Integer> Row = new ArrayList<>();
        ArrayList<Integer> Column = new ArrayList<>();
        String fourTile = displayEmotes(emoteInfo, "4Tile");
        String twoTile = displayEmotes(emoteInfo, "2Tile");
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                if (board[i][j].equals(tile.blankTile)){
                    Row.add(i);
                    Column.add(j);
                }
            }
        }

        Random randGen = new Random();
        int randomTile = randGen.nextInt(Row.size());
        boolean TileIsFour = randGen.nextInt(10) == 3;

        if (TileIsFour){
            board[Row.get(randomTile)][Column.get(randomTile)] = fourTile;
        } else {
            board[Row.get(randomTile)][Column.get(randomTile)] = twoTile;
        }
    }

    public GameOf2048(EventWaiter waiter) {
        this.waiter = waiter;
    }

    public void displayBoard(CommandContext ctx, String[][] board, HashMap<String, Long> emoteInfo){
        String userID = String.valueOf(ctx.getAuthor().getIdLong());
        String display = "";
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                display += board[i][j];
            }
            display += "\n";
        }
        String upButton = "upButton" + ctx.getAuthor().getIdLong();
        String leftButton = "leftButton" + ctx.getAuthor().getIdLong();
        String downButton = "downButton" + ctx.getAuthor().getIdLong();
        String rightButton = "rightButton" + ctx.getAuthor().getIdLong();
        String quitButton = "quitButton" + ctx.getAuthor().getIdLong();
        String solveButton = "solveButton" + ctx.getAuthor().getIdLong();

        ctx.getChannel().sendMessage("***PLEASE DON'T START A NEW GAME BEFORE FINISHING OR QUITTING THIS CURRENT GAME!***").queue();
        ctx.getChannel().sendMessage(display).setActionRows(
                ActionRow.of(Button.success(solveButton, "SOLVE"), Button.secondary(upButton, "⬆️"), Button.danger(quitButton, "QUIT")),
                ActionRow.of(Button.secondary(leftButton, "⬅️"),
                        Button.secondary(downButton, "⬇️"),
                        Button.secondary(rightButton, "➡️"))).queue();
    }

    public String updatedBoard(String[][] board){
        String updatedVer = "";
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                updatedVer += board[i][j];
            }
            updatedVer += "\n";
        }

        return updatedVer;
    }

    public void startGame(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, int score){
        String userID = String.valueOf(ctx.getAuthor().getIdLong());
        this.waiter.waitForEvent(
                ButtonClickEvent.class,
                (e) -> {
                    long nchannel = e.getChannel().getIdLong();
                    long nuser = e.getMember().getUser().getIdLong();
                    return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                },
                (e) -> {
                    String originalCopyOfBoard = updatedBoard(board);
                    if (e.getButton().getId().equalsIgnoreCase("upButton" + ctx.getAuthor().getIdLong())){
                        moveTilesUp(ctx, board, tile, emoteInfo, score);
                    } else if (e.getButton().getId().equalsIgnoreCase("leftButton" + ctx.getAuthor().getIdLong())){
                        moveTilesLeft(ctx, board, tile, emoteInfo, score);
                    } else if (e.getButton().getId().equalsIgnoreCase("downButton" + ctx.getAuthor().getIdLong())){
                        moveTilesDown(ctx, board, tile, emoteInfo, score);
                    } else if (e.getButton().getId().equalsIgnoreCase("rightButton" + ctx.getAuthor().getIdLong())){
                        moveTilesRight(ctx, board, tile, emoteInfo, score);
                    } else if (e.getButton().getId().equalsIgnoreCase("quitButton" + ctx.getAuthor().getIdLong())){
                        ctx.getChannel().sendMessage("You quit the game").queue();
                        return;
                    } else if (e.getButton().getId().equalsIgnoreCase("solveButton" + ctx.getAuthor().getIdLong())){
                        ctx.getChannel().sendMessage("This feature is not out yet! Sorry").queue();
                    } else {
                        String disqualifiedUser = "<@" + e.getMember().getUser().getIdLong() + ">";
                        ctx.getChannel().sendMessage(disqualifiedUser + " You were automatically disqualified from your game for " +
                                "trying to play someone else's game. Please don't do that EVER!!").queue();
                        return;
                    }

                    //generate a random tile
                    String newCopyOfBoard = updatedBoard(board);
                    if (originalCopyOfBoard.equals(newCopyOfBoard)){
                        //nothing moved
                    } else {
                        generateARandomTile(ctx, board, tile, emoteInfo);
                    }

                    //edit board
                    e.editMessage(updatedBoard(board)).queue();
                    if (gameIsWon(board, emoteInfo)){
                        ctx.getChannel().sendMessage("You reached 2048 and won!!! :partying_face:").queue();
                        return;
                    }
                    if (gameIsLost(board)){
                        ctx.getChannel().sendMessage("YOU LOST! Bruh.").queue();
                        return;
                    }
                    startGame(ctx, board, tile, emoteInfo, score);

                },
                90, TimeUnit.SECONDS,
                () -> {
                    ctx.getChannel().sendMessage("You took too long to respond. Game has ended").queue();
                    return;
                }
        );

    }

    public String mergeTiles(CommandContext ctx, String dominantTile, String shiftingTile, TwentyFourtyEightTiles tile,
                             HashMap<String, Long> emoteInfo){
        String twoTile = displayEmotes(emoteInfo, "2Tile");
        String fourTile = displayEmotes(emoteInfo, "4Tile");
        String eightTile = displayEmotes(emoteInfo, "8Tile");
        String sixteenTile = displayEmotes(emoteInfo, "16Tile");
        String thirtytwoTile = displayEmotes(emoteInfo, "32Tile");
        String sixtyfourTile = displayEmotes(emoteInfo, "64Tile");
        String onetwoeightTile = displayEmotes(emoteInfo, "128Tile");
        String twofivesixTile = displayEmotes(emoteInfo, "256Tile");
        String fivetwelveTile = displayEmotes(emoteInfo, "512Tile");
        String onezerotwofourTile = displayEmotes(emoteInfo, "1024Tile");
        String twentyfouryeightTile = displayEmotes(emoteInfo, "2048Tile");


        if (dominantTile.equals(twoTile)){
            return fourTile;
        } else if (dominantTile.equals(fourTile)){
            return eightTile;
        } else if (dominantTile.equals(eightTile)){
            return sixteenTile;
        } else if (dominantTile.equals(sixteenTile)){
            return thirtytwoTile;
        } else if (dominantTile.equals(thirtytwoTile)){
            return sixtyfourTile;
        } else if (dominantTile.equals(sixtyfourTile)){
            return onetwoeightTile;
        } else if (dominantTile.equals(onetwoeightTile)){
            return twofivesixTile;
        } else if (dominantTile.equals(twofivesixTile)){
            return fivetwelveTile;
        } else if (dominantTile.equals(fivetwelveTile)){
            return onezerotwofourTile;
        } else if (dominantTile.equals(onezerotwofourTile)){
            return twentyfouryeightTile;
        } else {
            System.out.println("Debug: mergeTiles failure");
            return "contact dev immediately if you see this message";
        }
    }



    public void moveTilesUp(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, int score){
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                String currentTile = board[i][j];
                if (currentTile.equalsIgnoreCase(tile.blankTile)){
                    //continue
                } else {
                    try {
                        int row = i;
                        int column = j;
                        while (true){
                            if (board[row - 1][column].equals(board[row][column])){
                                //merge tiles
                                String mergedValue = mergeTiles(ctx, board[row - 1][column], board[row][column], tile, emoteInfo);
                                board[row - 1][column] = mergedValue;
                                board[row][column] = tile.blankTile;
                                break;
                            } else if (board[row - 1][column].equals(tile.blankTile)){
                                //shift up tile
                                board[row - 1][column] = board[row][column];
                                board[row][column] = tile.blankTile;
                                row--;
                            } else {
                                //tiles don't match each other
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException exception){
                        //continue
                    }
                }
            }
        }
    }

    public void moveTilesLeft(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, int score){
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                String currentTile = board[i][j];
                if (currentTile.equalsIgnoreCase(tile.blankTile)){
                    //continue
                } else {
                    try {
                        int row = i;
                        int column = j;
                        while (true){
                            if (board[row][column - 1].equals(board[row][column])){
                                //merge tiles
                                String mergedValue = mergeTiles(ctx, board[row][column - 1], board[row][column], tile, emoteInfo);
                                board[row][column - 1] = mergedValue;
                                board[row][column] = tile.blankTile;
                                break;
                            } else if (board[row][column - 1].equals(tile.blankTile)){
                                //shift up tile
                                board[row][column - 1] = board[row][column];
                                board[row][column] = tile.blankTile;
                                column--;
                            } else {
                                //tiles don't match each other
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException exception){
                        //continue
                    }
                }
            }
        }
    }
    public void moveTilesDown(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, int score){
        for (int i = 3; i >= 0; i--){
            for (int j = 0; j < 4; j++){
                String currentTile = board[i][j];
                if (currentTile.equalsIgnoreCase(tile.blankTile)){
                    //continue
                } else {
                    try {
                        int row = i;
                        int column = j;
                        while (true){
                            if (board[row + 1][column].equals(board[row][column])){
                                //merge tiles
                                String mergedValue = mergeTiles(ctx, board[row + 1][column], board[row][column], tile, emoteInfo);
                                board[row + 1][column] = mergedValue;
                                board[row][column] = tile.blankTile;
                                break;
                            } else if (board[row + 1][column].equals(tile.blankTile)){
                                //shift up tile
                                board[row + 1][column] = board[row][column];
                                board[row][column] = tile.blankTile;
                                row++;
                            } else {
                                //tiles don't match each other
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException exception){
                        //continue
                    }
                }
            }
        }
    }

    public void moveTilesRight(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, int score){
        for (int i = 0; i < 4; i++){
            for (int j = 3; j >= 0; j--){
                String currentTile = board[i][j];
                if (currentTile.equalsIgnoreCase(tile.blankTile)){
                    //continue
                } else {
                    try {
                        int row = i;
                        int column = j;
                        while (true){
                            if (board[row][column + 1].equals(board[row][column])){
                                //merge tiles
                                String mergedValue = mergeTiles(ctx, board[row][column + 1], board[row][column], tile, emoteInfo);
                                board[row][column + 1] = mergedValue;
                                board[row][column] = tile.blankTile;
                                break;
                            } else if (board[row][column + 1].equals(tile.blankTile)){
                                //shift up tile
                                board[row][column + 1] = board[row][column];
                                board[row][column] = tile.blankTile;
                                column++;
                            } else {
                                //tiles don't match each other
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException exception){
                        //continue
                    }
                }
            }
        }
    }

    public boolean gameIsWon(String[][] board, HashMap<String, Long> emoteInfo){
        String twentyfourtyeightTile = displayEmotes(emoteInfo, "2048Tile");
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                if (board[i][j].equals(twentyfourtyeightTile)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean gameIsLost(String[][] board){
        int occupiedTiles = 0;
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                if (!board[i][j].equals(":white_large_square:")){
                    occupiedTiles++;
                }
            }
        }
        if (occupiedTiles != 16){
            return false;
        }

        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                try {
                    if (board[i][j].equals(board[i][j+1])){
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException exception){
                    //continue;
                }

                try {
                    if (board[i][j].equals(board[i][j-1])){
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException exception){
                    //continue;
                }

                try {
                    if (board[i][j].equals(board[i+1][j])){
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException exception){
                    //continue;
                }

                try {
                    if (board[i][j].equals(board[i-1][j])){
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException exception){
                    //continue;
                }
            }
        }
        return true;
    }

    public boolean allEmotesInstalled(CommandContext ctx, HashMap<String, Long> emoteInfo){
        int missingEmotes = 0;

        if (!emoteInfo.containsKey("2Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("4Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("8Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("16Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("32Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("64Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("128Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("256Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("512Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("1024Tile")){
            missingEmotes++;
        }
        if (!emoteInfo.containsKey("2048Tile")){
            missingEmotes++;
        }

        System.out.println(missingEmotes);

        if (missingEmotes > 0 && missingEmotes < 11){
            ctx.getChannel().sendMessage("You are missing some tile emotes. Install them with the command " +
                    "?install2048emotes").queue();
            return false;
        } else if (missingEmotes == 11){
            ctx.getChannel().sendMessage("You need to add custom emotes for the 2048 game to work!" +
                    " type ?install2048emotes to add them. Once you run this command, you will be able to play " +
                    "2048.").queue();
            return false;
        }

        return true;

    }

    public void enableRandomEngine(CommandContext ctx, String[][] board, TwentyFourtyEightTiles tile, HashMap<String, Long> emoteInfo, ButtonClickEvent e, int score){
        //timeInterval in milliseconds
        Random moveGenerator = new Random();
        long timeInterval = 500;
        Runnable runnable = new Runnable(){
            public void run(){
                while (true){
                    String originalCopyOfBoard = updatedBoard(board);
                    int move = moveGenerator.nextInt(4);
                    if (move == 0){
                        moveTilesUp(ctx, board, tile, emoteInfo, score);
                    } else if (move == 1){
                        moveTilesLeft(ctx, board, tile, emoteInfo, score);
                    } else if (move == 2){
                        moveTilesDown(ctx, board, tile, emoteInfo, score);
                    } else if (move == 3){
                        moveTilesRight(ctx, board, tile, emoteInfo, score);
                    }
                    //generate a random tile
                    String newCopyOfBoard = updatedBoard(board);
                    if (originalCopyOfBoard.equals(newCopyOfBoard)){
                        //nothing moved
                    } else {
                        generateARandomTile(ctx, board, tile, emoteInfo);
                    }

                    //edit board
                    e.editMessage(updatedBoard(board)).queue();
                    if (gameIsWon(board, emoteInfo)){
                        ctx.getChannel().sendMessage("You reached 2048 and won!!! :partying_face:").queue();
                        return;
                    }
                    if (gameIsLost(board)){
                        ctx.getChannel().sendMessage("YOU LOST! Bruh.").queue();
                        return;
                    }
                    try {
                        Thread.sleep(timeInterval);
                    } catch (InterruptedException interruptedException){
                        ctx.getChannel().sendMessage("Something went very wrong").queue();
                        interruptedException.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public void enableSolverEngine(String[][] board){

    }


    @Override
    public String getName() {
        return "2048";
    }

    @Override
    public String getHelp() {
        return "play 2048";
    }
}
