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
    public boolean denyIPAddressRequest(BufferedImage image, TextChannel channel) throws IOException {
        BufferedImage locationImage = ImageIO.read(new File("/Users/5kyle/Desktop/DiscordWolfram/locationimage.jpg"));
        int w1 = image.getWidth();
        int w2 = locationImage.getWidth();
        int h1 = image.getHeight();
        int h2 = locationImage.getHeight();
        if ((w1!=w2) || (h1!=h2)){
            return false;
        } else {
            long diff = 0;
            for (int j = 0; j < h1; j++){
                for (int i = 0; i < w1; i++){
                    int pixel1 = image.getRGB(i, j);
                    Color color1 = new Color(pixel1, true);
                    int r1 = color1.getRed();
                    int g1 = color1.getGreen();
                    int b1 = color1.getBlue();
                    int pixel2 = locationImage.getRGB(i, j);
                    Color color2 = new Color(pixel2, true);
                    int r2 = color2.getRed();
                    int g2 = color2.getGreen();
                    int b2 = color2.getBlue();
                    long data = Math.abs(r1-r2) + Math.abs(g1-g2) + Math.abs(b1-b2);
                    diff = diff + data;
                }
            }
            double avg = diff/(w1*h1*3);
            double percentage = (avg/255)*100;
            if (percentage == 0.39215686274509803){
                return true;
            }
        }

        return false;
    }

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
            String finalURL = "http://api.wolframalpha.com/v2/simple?appid=" + apiKey + "=" + searchResult + "%3F";

            File filePath = new File("/Users/5kyle/Desktop/DiscordWolfram/output.jpg");
            URL url = new URL(finalURL);
            BufferedImage image = ImageIO.read(url);
            if (denyIPAddressRequest(image, channel)){
                channel.sendMessage("you thought you could dox me bitch. your request has been denied").queue();
                return;
            }
            ImageIO.write(image, "jpg", new File("/Users/5kyle/Desktop/DiscordWolfram/output.jpg"));
            channel.sendMessage("Here is the answer to your question: ").addFile(filePath).queue();

            //EmbedBuilder info = EmbedUtils.embedImageWithTitle(String.join(" ", args), finalURL, finalURL);
            //info.setFooter("Inutile || Image Won't Load? Click on the link!");
            //channel.sendMessageEmbeds(info.build()).queue();
        } catch (IOException pp){
            channel.sendMessage("The engine did not understand your question. Please ask something math or science related this isn't google").queue();
        }

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