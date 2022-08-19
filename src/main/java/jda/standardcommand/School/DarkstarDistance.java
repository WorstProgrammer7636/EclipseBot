package jda.standardcommand.School;

import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;

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
            String[] arguments = {"0.000621371", "(", "integral_0^210.665", "231.5dx"};
            searchResult = String.join("+", arguments);
        } else if (seconds <= 371){
            String[] arguments = {"0.000621371", "(", "integral_0^210.665", "231.5dx", "plus", "integral_211^371", "(20", "(x", "-", "210.665)", "plus", "231.5)", "dx"};
            searchResult = String.join("+", arguments);
        } else {
            String[] arguments = {"0.000621371", "(", "integral_0^210.665", "231.5dx", "plus", "integral_211^371", "(20", "(x", "-", "210.665)", "plus", "231.5)", "dx", "plus", "integral_371^"+seconds, "3430dx)"};
            searchResult = String.join("+", arguments);
        }

        String finalURL = "https://api.wolframalpha.com/v1/result?appid=" + apiKey + "=" + searchResult + "%3F";
        URL url = new URL(finalURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String answer;
        while ((answer = in.readLine()) != null) {
            if (answer.length() > 1999){
                ctx.getChannel().sendMessage("The question you asked provoked a response of " +
                        "more than 2000 characters, the discord limit. Please ask" +
                        " a different question").queue();
                return;
            }
            ctx.getChannel().sendMessage("The hypersonic darkstar would travel " + answer + " miles if it flew " + seconds + " seconds!").queue();
        }
        in.close();
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
