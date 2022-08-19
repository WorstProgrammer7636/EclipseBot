package jda.standardcommand.School;

import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;

public class DarkstarDistance implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        if (ctx.getArgs().isEmpty()){
            ctx.getChannel().sendMessage("Enter an amount of seconds for the darkstar to travel! Example: ?darkstar 600").queue();
            return;
        } else if (ctx.getArgs().size() > 1){
            ctx.getChannel().sendMessage("Not a valid input").queue();
        }

        double seconds = 0;
        try {
            seconds = Double.parseDouble(ctx.getArgs().get(0));
        } catch (NumberFormatException numberFormatException){
            ctx.getChannel().sendMessage("You must enter a positive number for the amount of seconds for the darkstar to fly! Example: " +
                    "?darkstar 600").queue();
            return;
        }
        if (seconds < 60){
            ctx.getChannel().sendMessage("Make sure the darkstar gets a chance to lift off man. At least 60 seconds fly time. Example: " +
                    "?darkstar 600").queue();
            return;
        }

        String apiKey = Config.get("WOLFRAMAPI");
        String searchResult = "";
        if (seconds <= 210.665){
            String[] arguments = {"0.000621371", "(", "integral_0^" + seconds, "231.5dx"};
            searchResult = String.join("+", arguments);
        } else if (seconds <= 371){
            String[] arguments = {"0.000621371", "(", "integral_0^210.665", "231.5dx", "plus", "integral_211^" + seconds, "(20", "(x", "-", "210.665)", "plus", "231.5)", "dx"};
            searchResult = String.join("+", arguments);
        } else {
            String[] arguments = {"0.000621371", "(", "integral_0^210.665", "231.5dx", "plus", "integral_211^371", "(20", "(x", "-", "210.665)", "plus", "231.5)", "dx", "plus", "integral_371^" + seconds, "3430dx)"};
            searchResult = String.join("+", arguments);
        }

        String finalURL = "https://api.wolframalpha.com/v1/result?appid=" + apiKey + "=" + searchResult + "%3F";
        URL url = new URL(finalURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String answer;
        StringBuilder miles = new StringBuilder();
        while ((answer = in.readLine()) != null) {
            if (answer.length() > 1999){
                ctx.getChannel().sendMessage("The question you asked provoked a response of " +
                        "more than 2000 characters, the discord limit. Please ask" +
                        " a different question").queue();
                return;
            }
            miles.append(answer);
        }
        in.close();

        EmbedBuilder result = new EmbedBuilder();
        result.setTitle("Your results:");
        result.setDescription("The hypersonic darkstar would travel " + miles + " miles if it flew " + seconds + " seconds!\n" +
                "");
        result.setFooter("Learn how it works here!: https://docs.google.com/document/d/13wZznNwV747Kisrv5-cww1hVKFTKaxASBlfILGN4MHE/edit \n" +
                "\n" +
                "Watch the scene: https://www.youtube.com/watch?v=8IeqOZt72ZU&ab_channel=Maxwell");
        result.setThumbnail("https://www.thedrive.com/uploads/2022/06/02/Darkstar-Lockheed-TOPGUN.jpg?auto=webp&auto=webp&optimize=high&quality=70&width=1920");
        result.setColor(Color.MAGENTA);
        ctx.getChannel().sendMessage(result.build()).queue();
    }

    @Override
    public String getName() {
        return "darkstar";
    }

    @Override
    public String getHelp() {
        return null;
    }
}
