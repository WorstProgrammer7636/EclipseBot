package jda.standardcommand.games;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.Random;

public class FlipCoin implements ICommand {


    @Override
    public void handle(CommandContext ctx) throws IOException {
        TextChannel channel = ctx.getChannel();
        Random generator = new Random();
        int result = generator.nextInt(2);
        if (result == 0){
            channel.sendMessage("Heads!").queue();
        } else {
            channel.sendMessage("Tails").queue();
        }
    }

    @Override
    public String getName() {
        return "flipcoin";
    }

    @Override
    public String getHelp() {
        return "flip a coin to get heads or tails. Usage: ?flipcoin";
    }
}
