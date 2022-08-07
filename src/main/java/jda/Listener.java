package jda;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Listener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;

    public Listener (EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        long sendGuild = Long.parseLong("1005738790029099058");
        JDA bot = event.getJDA();
        Objects.requireNonNull(bot.getGuildById(sendGuild)).getTextChannelsByName("bot-status", true).get(0)
                .sendMessage("Bot is on! Bot was activated at " + dtf.format(now)).queue();
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
        try {
            initialPrefixCheck();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Initial prefix check completed");
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()) {
            return;
        }
        final long guildId = event.getGuild().getIdLong();
        String prefix = TempDesign.PREFIXES.computeIfAbsent(guildId, (id) -> Config.get("prefix"));
        String raw = event.getMessage().getContentRaw();

        if (raw.equalsIgnoreCase(prefix + "terminate")
                && event.getAuthor().getId().equals(Config.get("ownerid"))) {
            LOGGER.info("Shutting Down the bot");
            event.getChannel().sendMessage("Bot Successfully Disabled!").queue();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            JDA bot = event.getJDA();
            long sendGuild = Long.parseLong("1005738790029099058");

            Objects.requireNonNull(bot.getGuildById(sendGuild)).getTextChannelsByName("bot-status", true).get(0)
                    .sendMessage("Bot is off! Bot was deactivated at " + dtf.format(now)).queue();
            startTimer();
            event.getJDA().shutdown();
            BotCommons.shutdown(event.getJDA());

            return;

        }


        if (raw.startsWith(prefix)) {
            try {
                manager.handle(event, prefix);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void startTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    public void initialPrefixCheck() throws IOException {
        ArrayList<Long> IDS = new ArrayList<Long>();
        ArrayList<String> prefixes = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("prefixes"));
        StringTokenizer st = null;
        String line;
        while ((line = br.readLine()) != null) {
            st = new StringTokenizer(line);
            long currentID = (Long.parseLong(st.nextToken()));
            String currentprefix = (st.nextToken());
            IDS.add(currentID);
            prefixes.add(currentprefix);

            TempDesign.PREFIXES.put(currentID, currentprefix);
        }


        br.close();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("prefixes")));
        for (int i = 0; i < IDS.size(); i++) {
            pw.print(IDS.get(i));
            pw.print(" ");
            pw.print(prefixes.get(i));
            pw.println();

        }
        pw.close();
    }

}

