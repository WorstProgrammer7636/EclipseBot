package jda.standardcommand.gamesSetupFiles;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

public class InstallEmotes implements ICommand {

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
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


        ctx.getGuild().createEmote("2Tile", twoIcon).queue();
        ctx.getGuild().createEmote("4Tile", fourIcon).queue();
        ctx.getGuild().createEmote("8Tile", eightIcon).queue();
        ctx.getGuild().createEmote("16Tile", sixteenIcon).queue();
        ctx.getGuild().createEmote("32Tile", thirtytwoIcon).queue();
        ctx.getGuild().createEmote("64Tile", sixtyfourIcon).queue();
        ctx.getGuild().createEmote("128Tile", onetwentyeightIcon).queue();
        ctx.getGuild().createEmote("256Tile", twofiftysixIcon).queue();
        ctx.getGuild().createEmote("512Tile", fivetwelveIcon).queue();
        ctx.getGuild().createEmote("1024Tile", onezerotwofourIcon).queue();
        ctx.getGuild().createEmote("2048Tile", twentyfourtyeightIcon).queue();

        ctx.getChannel().sendMessage("Finished installing emotes").queue();
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
