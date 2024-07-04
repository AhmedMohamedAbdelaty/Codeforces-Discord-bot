package bot.util;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedBuilderUtil {

    public static EmbedBuilder createDefaultEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE); // Set a default color for all embeds
        // Add more default configurations here
        return embed;
    }

    public static EmbedBuilder createErrorEmbed(String errorMessage) {
        EmbedBuilder embed = createDefaultEmbed();
        embed.setColor(Color.RED); // Error messages can be red
        embed.setDescription(errorMessage);
        return embed;
    }
}