package jda.standardcommand;

import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Random;

public class NewColorCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        Random random = new Random();
        final float hue = random.nextFloat();
        final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
        Color color = Color.getHSBColor(hue, saturation, luminance);
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setFooter("Inutile")
        );
        final TextChannel channel = ctx.getChannel();
        channel.sendMessage("Embed color changed!").queue();
    }

    @Override
    public String getName() {
        return "newcolor";
    }

    @Override
    public String getHelp() {
        return "Changes color of the side border in embeds" + "\n Usage: `?newcolor`";
    }
}
