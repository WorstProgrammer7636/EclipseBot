package jda.standardcommand.gamesSetupFiles;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

public class InstallEmotes implements ICommand {

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        try {
            Icon twoIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/TWOFILE.png"));
            Icon fourIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/FOURTILE.png"));
            Icon eightIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/EIGHTTILE.png"));
            Icon sixteenIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/SIXTEENTILE.png"));
            Icon thirtytwoIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/THIRTYTWOTILE.png"));
            Icon sixtyfourIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/SIXTYFOURTILE.png"));
            Icon onetwentyeightIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/128TILE.png"));
            Icon twofiftysixIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/256TILE.png"));
            Icon fivetwelveIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/512TILE.png"));
            Icon onezerotwofourIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/1024TILE.png"));
            Icon twentyfourtyeightIcon = Icon.from(new File("./src/main/java/jda/standardcommand/gamesSetupFiles/TwentyFourtyEightEmotes/2048TILE.png"));
            List<Emote> emotes = ctx.getGuild().getEmotes();
            HashMap<String, Long> emoteInfo = new HashMap<>();
            for (int i = 0; i < emotes.size(); i++){
                emoteInfo.put(emotes.get(i).getName(), emotes.get(i).getIdLong());
            }

            if (!emoteInfo.containsKey("2Tile")){
                ctx.getGuild().createEmote("2Tile", twoIcon).queue();
            }

            if (!emoteInfo.containsKey("4Tile")){
                ctx.getGuild().createEmote("4Tile", fourIcon).queue();
            }
            if (!emoteInfo.containsKey("8Tile")){
                ctx.getGuild().createEmote("8Tile", eightIcon).queue();
            }
            if (!emoteInfo.containsKey("16Tile")){
                ctx.getGuild().createEmote("16Tile", sixteenIcon).queue();
            }
            if (!emoteInfo.containsKey("32Tile")){
                ctx.getGuild().createEmote("32Tile", thirtytwoIcon).queue();
            }
            if (!emoteInfo.containsKey("64Tile")){
                ctx.getGuild().createEmote("64Tile", sixtyfourIcon).queue();
            }
            if (!emoteInfo.containsKey("128Tile")){
                ctx.getGuild().createEmote("128Tile", onetwentyeightIcon).queue();
            }
            if (!emoteInfo.containsKey("256Tile")){
                ctx.getGuild().createEmote("256Tile", twofiftysixIcon).queue();
            }
            if (!emoteInfo.containsKey("512Tile")){
                ctx.getGuild().createEmote("512Tile", fivetwelveIcon).queue();
            }
            if (!emoteInfo.containsKey("1024Tile")){
                ctx.getGuild().createEmote("1024Tile", onezerotwofourIcon).queue();
            }
            if (!emoteInfo.containsKey("2048Tile")){
                ctx.getGuild().createEmote("2048Tile", twentyfourtyeightIcon).queue();
            }

            ctx.getChannel().sendMessage("Finished installing emotes. You can now run the ?2048 command!").queue();
        } catch (InsufficientPermissionException exception){
            ctx.getChannel().sendMessage("I don't have permission to perform this command!").queue();
            return;
        } catch (Exception e){
            ctx.getChannel().sendMessage("I can't perform this action!! Please make sure you have at least" +
                    " 11 available slots for me to install my emotes in your server. You can do this by deleting some server emotes " +
                    "to free up space or you can boost your server to increase your server emote capacity.").queue();
        }
    }

    @Override
    public String getName() {
        return "install2048emotes";
    }

    @Override
    public String getHelp() {
        return "Install all the custom emotes necessary in order to play 2048";
    }
}
