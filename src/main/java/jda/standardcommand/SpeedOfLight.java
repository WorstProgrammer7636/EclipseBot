package jda.standardcommand;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class SpeedOfLight implements ICommand {
    private final EventWaiter waiter;

    public SpeedOfLight(EventWaiter waiter) {
        this.waiter = waiter;
    }

    public void askForTime(CommandContext ctx){
        ctx.getChannel().sendMessage("Enter a time in seconds from your perspective").queue((message -> {
            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        try {
                            BigDecimal ZERO = new BigDecimal("0");
                            BigDecimal ownTime = new BigDecimal(e.getMessage().getContentRaw());
                            if (ownTime.compareTo(ZERO) < 0){
                                ctx.getChannel().sendMessage("No negative time allowed").queue();
                                return;
                            }

                            getSpeed(ctx, ownTime);
                        } catch (Exception exception){
                            ctx.getChannel().sendMessage("Not a valid input. sorry").queue();
                            return;
                        }
                    },
                    30, TimeUnit.SECONDS,
                    () -> ctx.getChannel().sendMessage("Took too long to respond").queue()
            );
        }));


    }

    public void getSpeed(CommandContext ctx, BigDecimal ownTime){
        ctx.getChannel().sendMessage("What is your speed? (We will ask for units later)").
                queue((message) -> {

            this.waiter.waitForEvent(
                    GuildMessageReceivedEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        try {
                            BigDecimal speed = new BigDecimal(e.getMessage().getContentRaw());
                            askForUnits(ctx, ownTime, speed);
                        } catch (Exception exception){
                            ctx.getChannel().sendMessage("Sorry. Not a valid input").queue();
                            return;
                        }
                    },
                    30, TimeUnit.SECONDS,
                    () -> ctx.getChannel().sendMessage("Took too long to respond").queue()
            );
        });
    }

    public void askForUnits(CommandContext ctx, BigDecimal ownTime, BigDecimal speed) {
        ctx.getChannel().sendMessage("Pick your speed unit").
                setActionRows(ActionRow.of(Button.primary("1", "meters/sec"),
                        Button.primary("2", "km/hr"), Button.primary("3", "miles/hr"),
                        Button.success("4", "miles/sec"), Button.success("5", "Mach")),
                        ActionRow.of(Button.secondary("6", "knots"),
                                Button.secondary("7", "% of light speed"), Button.danger("QUIT", "QUIT"))).queue((message) -> {

            this.waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (e) -> {
                        long nchannel = e.getChannel().getIdLong();
                        long nuser = e.getMember().getUser().getIdLong();
                        return ctx.getChannel().getIdLong() == nchannel && nuser == ctx.getMember().getIdLong();
                    },
                    (e) -> {
                        String buttonID = e.getButton().getId();
                        BigDecimal speedInLight = speed;
                        BigDecimal originalSpeed = speed;

                        MathContext mc = new MathContext(30);
                        BigDecimal IDONE = new BigDecimal("299792458");
                        BigDecimal IDTWO = new BigDecimal("0.00000000092657");
                        BigDecimal IDTHREE = new BigDecimal("0.0000000014912");
                        BigDecimal IDFOUR = new BigDecimal("186300");
                        BigDecimal IDFIVE = new BigDecimal("874030.489796");
                        BigDecimal IDSIX = new BigDecimal("582749918");
                        if (buttonID.equals("1")){
                             speedInLight = speedInLight.divide(IDONE, mc);
                        } else if (buttonID.equals("2")){
                            speedInLight = speedInLight.multiply(IDTWO, mc);
                        } else if (buttonID.equals("3")){
                            speedInLight = speedInLight.multiply(IDTHREE, mc);
                        } else if (buttonID.equals("4")){
                            speedInLight = speedInLight.divide(IDFOUR, mc);
                        } else if (buttonID.equals("5")){
                            speedInLight = speedInLight.divide(IDFIVE, mc);
                        } else if (buttonID.equals("6")){
                            speedInLight = speedInLight.divide(IDSIX, mc);
                        } else if (buttonID.equals("7")){
                            if (speedInLight.compareTo(BigDecimal.ONE) >= 0){
                                ctx.getChannel().sendMessage("You can't have a speed of light greater than 1.").queue();
                                return;
                            }
                        } else {
                            ctx.getChannel().sendMessage("YOU QUIT");
                            return;
                        }

                        calculateTimeDilation(ctx, ownTime, speedInLight, originalSpeed, e.getButton().getLabel());
                    },
                    30, TimeUnit.SECONDS,
                    () -> ctx.getChannel().sendMessage("Took too long to respond").queue()
            );
        });
    }

    public void calculateTimeDilation(CommandContext ctx, BigDecimal ownTime, BigDecimal speed, BigDecimal originalSpeed, String units){
        BigDecimal result;
        try {
            BigDecimal denomNum = speed.pow(2);
            BigDecimal denom = BigDecimal.ONE.subtract(denomNum);
            MathContext mc = new MathContext(25);
            denom = denom.sqrt(mc);
            result = ownTime.divide(denom, mc);
        } catch (ArithmeticException arithmeticException){
            ctx.getChannel().sendMessage("Remember that the laws of the universe state that you can't go at a speed faster than light!").queue();
            return;
        }

        ctx.getChannel().sendMessage("Result: " + result + " seconds would have passed on Earth if you spent " + ownTime + " seconds" +
                "(your frame of time) travelling at " + originalSpeed + " " + units).queue();
    }
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        if (!ctx.getArgs().isEmpty()){
            ctx.getChannel().sendMessage("No arguments please").queue();
            return;
        }

        askForTime(ctx);

    }



    @Override
    public String getName() {
        return "spl";
    }

    @Override
    public String getHelp() {
        return null;
    }
}
