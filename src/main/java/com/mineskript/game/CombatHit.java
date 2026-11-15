package com.mineskript.game;

import com.mineskript.lang.ast.EntityValue;

public record CombatHit(String damageType, EntityValue attacker) {
}
