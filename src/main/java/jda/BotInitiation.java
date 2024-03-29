package jda;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.EnumSet;
import java.util.Random;

public class BotInitiation {
    static EventWaiter waiter = new EventWaiter();

    public static void main(String[] args) throws LoginException, InterruptedException {
        WebUtils.setUserAgent("Mozilla/5.0 Eclipse#6243 / spicyburrito#0001");
        Random random = new Random();
        final float hue = random.nextFloat();
        final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
        Color color = Color.getHSBColor(hue, saturation, luminance);
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setFooter("Eclipse")
        );
        JDABuilder jdaBuilder = JDABuilder.createDefault(Config.get("token"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_EMOJIS
                        ).setDisabledCacheFlags(EnumSet.of(
                        CacheFlag.ONLINE_STATUS
                ))
                .enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.ALL);
        JDA jda = null;
        try {
            jda = jdaBuilder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
        assert jda != null;
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.watching("?help"));
        jda.addEventListener(new Listener(waiter), waiter);
    }

}

