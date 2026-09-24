package com.mineskript.client.movement;

import com.mineskript.client.movement.elements.CondKeyHeld;
import com.mineskript.client.movement.elements.EffAttackUse;
import com.mineskript.client.movement.elements.EffKey;
import com.mineskript.client.movement.elements.EffLookAt;
import com.mineskript.client.movement.elements.EffSetRotation;
import com.mineskript.client.movement.elements.ExprPitch;
import com.mineskript.client.movement.elements.ExprYaw;
import com.mineskript.client.movement.elements.MovementEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** Moving and looking around, keys and mouse buttons. */
public final class MovementModule implements SyntaxModule {
    @Override
    public String name() {
        return "movement";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        MovementEvents.register(registry);

        ExprYaw.register(registry);
        ExprPitch.register(registry);

        CondKeyHeld.register(registry);

        EffAttackUse.register(registry);
        EffKey.register(registry);
        EffLookAt.register(registry);
        EffSetRotation.register(registry);
    }
}
