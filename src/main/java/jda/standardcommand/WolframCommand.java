package jda.standardcommand;

import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;

public class WolframCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) throws IOException {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        if (args.isEmpty()) {
            channel.sendMessage("Please ask a question").queue();
            return;
        }
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).replace("+", "plus"));
        }
        String searchResult = String.join("+", args);
        String apiKey = Config.get("WOLFRAMAPI");
        String finalURL = "http://api.wolframalpha.com/v2/simple?appid=" + apiKey + "=" + searchResult + "%3F";

        File filePath = new File("/Users/5kyle/Desktop/DiscordWolfram/output.jpg");
        URL url = new URL(finalURL);
        BufferedImage image = ImageIO.read(url);
        ImageIO.write(image, "jpg", new File("/Users/5kyle/Desktop/DiscordWolfram/output.jpg"));
        channel.sendMessage("Here is the answer to your question: ").addFile(filePath).queue();

        //EmbedBuilder info = EmbedUtils.embedImageWithTitle(String.join(" ", args), finalURL, finalURL);
        //info.setFooter("Inutile || Image Won't Load? Click on the link!");
        //channel.sendMessageEmbeds(info.build()).queue();
    }

    @Override
    public String getName() {
        return "wolfram";
    }

    @Override
    public String getHelp() {
        return "Ask Wolphram Alpha A Question! \n Usage: `?wolfram <query>`";
    }
}