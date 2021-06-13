package com.lojfacens.pitchy.util;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;

public class DisUtils {

  private DisUtils() { }

  public static final String TITLE_EXCEPTION = "❌ Oops";
  public static final String FOOTER_DEFAULT = "🍑 Pitchy";

  public static final Color COLOR_PINK = Color.decode("#e35f8d");

  public static EmbedBuilder responseEmbedBuilder() {
    return new EmbedBuilder()
        .setColor(COLOR_PINK)
        .setFooter(FOOTER_DEFAULT);
  }

}
