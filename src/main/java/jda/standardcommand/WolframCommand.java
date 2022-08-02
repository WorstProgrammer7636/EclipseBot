package jda.standardcommand;

import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;

public class WolframCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        if (args.isEmpty()) {
            channel.sendMessage("Please ask a question").queue();
            return;
        }


        channel.sendMessage("Initiating engine... solving problem... please wait a few seconds").queue();
        try {
            for (int i = 0; i < args.size(); i++) {
                args.set(i, args.get(i).replace("+", "plus"));
            }
            String searchResult = String.join("+", args);
            String apiKey = Config.get("WOLFRAMAPI");
            String finalURL = "https://api.wolframalpha.com/v1/result?appid=" + apiKey + "=" + searchResult + "%3F";
            URL url = new URL(finalURL);
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() > 1999){
                    channel.sendMessage("The question you asked provoked a response of " +
                            "more than 2000 characters, the discord limit. Please ask" +
                            " a different question").queue();
                    return;
                }
                channel.sendMessage("ANSWER IS: " + line).queue();
            }
            in.close();
        } catch (IOException pp){
            pp.printStackTrace();
            channel.sendMessage("The engine could not find an answer to that question. Please ask something else. Example: -wolfram integrate 3sin(x)").queue();
        }

    }

    @Override
    public String getName() {
        return "wolfram";
    }

    @Override
    public String getHelp() {
        return "Ask the powerful Wolphram AI A Question! \n Usage: `?wolfram <your request>`. " +
                "Example: ?wolfram integrate x/(x^2 - 4)";
    }
}