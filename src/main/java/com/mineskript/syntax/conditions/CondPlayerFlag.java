package com.mineskript.syntax.conditions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class CondPlayerFlag implements Condition {
    private static final String FLAGS =
            "(water:in [the] water|lava:in [the] lava|fire:on fire|flying:flying|sleeping:sleeping"
            + "|blocking:blocking|using:using [an] item|swimming:swimming|invisible:invisible|riding:riding)";
    private static final Map<String, Predicate<GameBridge>> READERS = readers();

    private final Predicate<GameBridge> reader;
    private final boolean negate;

    private CondPlayerFlag(Predicate<GameBridge> reader, boolean negate) {
        this.reader = reader;
        this.negate = negate;
    }

    private static Map<String, Predicate<GameBridge>> readers() {
        Map<String, Predicate<GameBridge>> readers = new LinkedHashMap<>();
        readers.put("water", GameBridge::isInWater);
        readers.put("lava", GameBridge::isInLava);
        readers.put("fire", GameBridge::isOnFire);
        readers.put("flying", GameBridge::isFlying);
        readers.put("sleeping", GameBridge::isSleeping);
        readers.put("blocking", GameBridge::isBlocking);
        readers.put("using", GameBridge::isUsingItem);
        readers.put("swimming", GameBridge::isSwimming);
        readers.put("invisible", GameBridge::isInvisible);
        readers.put("riding", game -> game.vehicle() != null);
        return Collections.unmodifiableMap(readers);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false), "%player% (is|are) " + FLAGS);
        registry.addCondition((match, scope) -> create(match, true), "%player% (isn't|is not|aren't|are not) " + FLAGS);
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        for (Map.Entry<String, Predicate<GameBridge>> entry : READERS.entrySet()) {
            if (match.has(entry.getKey())) {
                return Optional.of(new CondPlayerFlag(entry.getValue(), negate));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean test(Context context) {
        return negate != reader.test(context.world());
    }
}
