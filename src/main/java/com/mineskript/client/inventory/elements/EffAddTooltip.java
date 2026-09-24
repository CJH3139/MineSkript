package com.mineskript.client.inventory.elements;

import com.mineskript.client.TextColors;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.TooltipLines;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.List;
import java.util.Optional;

@Name("Add To Tooltip")
@Description({"Adds lines to the tooltip of the hovered item, inside an on item tooltip trigger. Only you see them: nothing about the item changes, for you or anyone else. Lines are added at the bottom, or just under the item name with to the top of the tooltip. Each text in a list becomes its own line.",
        "Colour codes work with & or the section sign: &0 to &9 and &a to &f are the 16 colours (&c red, &a green, &6 gold, &7 grey), &l bold, &o italic, &n underline, &m strikethrough, &k obfuscated and &r resets. At most 64 lines are added to one tooltip.",
        "It can only be used in an on item tooltip trigger; anywhere else the script does not load."})
@Examples({"on item tooltip:",
        "	add \"&7stack of %count of event-item%\" to the tooltip",
        "	if level of enchantment \"sharpness\" on event-item is more than 4:",
        "		add \"&c&lsharp!\" to the top of the tooltip"})
@Since("1.0.0-alpha.9")
public final class EffAddTooltip implements Statement {
    private final int line;
    private final Expression lines;
    private final boolean top;

    private EffAddTooltip(int line, Expression lines, boolean top) {
        this.line = line;
        this.lines = lines;
        this.top = top;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffAddTooltip::create,
                "add %strings% to [the] tooltip",
                "add %strings% to [the] (top|start) of [the] tooltip");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (!(scope.event() instanceof Event.State state) || !state.name().equals("tooltip")) {
            throw new SyntaxException("add to tooltip only works in an on item tooltip trigger");
        }
        return Optional.of(new EffAddTooltip(scope.line(), match.slot(0), match.patternIndex() == 1));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        TooltipLines tooltip = (TooltipLines) context.eventValue(TooltipLines.KEY);
        Object value = lines.evaluate(context);
        List<?> texts = value instanceof List<?> list ? list : List.of(value);
        for (Object text : texts) {
            String colored = TextColors.colored(Converters.toText(text, context));
            if (top) {
                tooltip.addTop(colored);
            } else {
                tooltip.addBottom(colored);
            }
        }
        return Flow.CONTINUE;
    }
}
