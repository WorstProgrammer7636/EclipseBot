package jda.standardcommand.admin;

import jda.Config;
import jda.TempDesign;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SetPrefixCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        final Member member = ctx.getMember();

        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You must have the Manage Server permission to use this command!").queue();
            return;
        }
        if (args.isEmpty()) {
            channel.sendMessage("Missing a new prefix to use").queue();
            return;
        }

        final String newPrefix = String.join("", args);
        TempDesign.PREFIXES.put(ctx.getGuild().getIdLong(), newPrefix);

        check(ctx.getGuild().getIdLong());
        change(ctx.getGuild().getIdLong(), newPrefix);
        channel.sendMessageFormat("New Prefix set to `%s`", newPrefix).queue();
    }

    @Override
    public String getName() {
        return "setprefix";
    }

    @Override
    public String getHelp() {
        return "Sets the prefix for the server\n Usage: `?setprefix <prefix>`";
    }

    public void check(long id) throws IOException {
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
        if (!IDS.contains(id)) {
            IDS.add(id);
            prefixes.add(Config.get("prefix"));
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

    public void change(long id, String newprefix) throws NumberFormatException, IOException {
        ArrayList<Long> IDS = new ArrayList<Long>();
        ArrayList<String> prefixes = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("prefixes"));
        StringTokenizer st = null;
        String line;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            st = new StringTokenizer(line);
            IDS.add(Long.parseLong(st.nextToken()));
            if (IDS.contains(id) && !found) {
                prefixes.add(newprefix);
                TempDesign.PREFIXES.put(id, newprefix);
                found = true;
            } else {
                prefixes.add(st.nextToken());
            }
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

//done
}
