package com.mineskript;

import com.mineskript.script.MessageLine;
import com.mineskript.script.Messages;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class MineSkriptMessages {
    private MineSkriptMessages() {
    }

    public static void send(FabricClientCommandSource source, List<MessageLine> lines) {
        boolean first = true;
        for (MessageLine line : lines) {
            source.sendFeedback(first ? component(line) : continuation(line));
            first = false;
        }
    }

    public static Component component(MessageLine line) {
        return Component.empty()
                .append(prefix())
                .append(Component.literal(" "))
                .append(body(line));
    }

    public static Component continuation(MessageLine line) {
        return Component.empty()
                .append(Component.literal(indent()))
                .append(body(line));
    }

    private static MutableComponent prefix() {
        return Component.literal(Messages.PREFIX).setStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withBold(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("MineSkript, type /ms help"))));
    }

    private static String indent() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.font == null) {
            return Messages.INDENT;
        }
        Font font = client.font;
        int space = font.width(" ");
        if (space <= 0) {
            return Messages.INDENT;
        }
        int wanted = font.width(prefix()) + space;
        return " ".repeat((wanted + space - 1) / space);
    }

    private static Component body(MessageLine line) {
        return Component.literal(line.text()).withStyle(colour(line.kind()));
    }

    private static ChatFormatting colour(MessageLine.Kind kind) {
        return switch (kind) {
            case SUCCESS -> ChatFormatting.GREEN;
            case WARNING -> ChatFormatting.YELLOW;
            case ERROR -> ChatFormatting.RED;
            case DETAIL -> ChatFormatting.DARK_GRAY;
            case INFO -> ChatFormatting.WHITE;
        };
    }
}
