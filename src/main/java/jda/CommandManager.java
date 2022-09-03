package jda;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import jda.standardcommand.*;
import jda.standardcommand.School.DarkstarDistance;
import jda.standardcommand.School.GetStudentInfo;
import jda.standardcommand.admin.FlushServerEmotes;
import jda.standardcommand.admin.SetPrefixCommand;
import jda.standardcommand.games.*;
import jda.standardcommand.gamesSetupFiles.InstallEmotes;
import jda.standardcommand.music.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager(EventWaiter waiter) {
        addCommand(new PingCommand());
        addCommand(new HelpCommand(this));
        addCommand(new HasteCommand());
        addCommand(new KickCommand());
        addCommand(new MemeCommand());
        addCommand(new NewColorCommand());
        addCommand(new WebhookCommand());
        addCommand(new WolframCommand());
        addCommand(new JokeCommand());
        //addCommand(new InstagramCommand());
        addCommand(new SetPrefixCommand());
        addCommand(new MinecraftCommand());
        addCommand(new JoinCommand());
        addCommand(new PlayCommand());
        addCommand(new ClearCommand());
        addCommand(new SkipCommand());
        addCommand(new PauseCommand());
        addCommand(new ResumeCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new QueueCommand(waiter));
        addCommand(new RepeatCommand());
        addCommand(new QueueLoopCommand());
        addCommand(new LeaveCommand());
        addCommand(new Translate());
        addCommand(new TicTacToe(waiter));
        addCommand(new ShuffleCommand());
        addCommand(new ConnectFour(waiter));
        addCommand(new WordBomb(waiter));
        //addCommand(new StrategicTicTacToe(waiter));
        //addCommand(new Monopoly(waiter));
        addCommand(new FlipCoin());
        addCommand(new GetPlatform());
        addCommand(new getInviteLink());
        addCommand(new RPS(waiter));
        addCommand(new DatabaseHandling());
        addCommand(new AdminExec());
        addCommand(new Hangman(waiter));
        addCommand(new VolumeCommand(waiter));
        addCommand(new GameOf2048(waiter));
        addCommand(new FlushServerEmotes());
        addCommand(new InstallEmotes());
        addCommand(new MultiplayerConnectFour(waiter));
        addCommand(new GetStudentInfo());
        addCommand(new DarkstarDistance());
        addCommand(new SpeedOfLight(waiter));
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("A command with this name is already present");
        }

        commands.add(cmd);
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();
        for (ICommand cmd : this.commands) {
            if (cmd.getName().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }
        return null;
    }

    void handle(GuildMessageReceivedEvent event, String prefix) throws IOException, GeneralSecurityException, ClassNotFoundException {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "").split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
            System.out.println(ctx.getGuild().getName() + ": " + ctx.getAuthor().getName() + ": " + cmd);
        }
    }
}
