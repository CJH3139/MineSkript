package com.mineskript.lang.ast;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum DamageCause {
    BLOCK_EXPLOSION(List.of("block explosion")),
    CAMPFIRE(List.of("campfire")),
    CONTACT(List.of("contact")),
    CRAMMING(List.of("cramming")),
    CUSTOM(List.of("unknown", "custom")),
    DRAGON_BREATH(List.of("dragon breath", "dragon's breath", "dragon fire")),
    DROWNING(List.of("drowning", "drown")),
    DRYOUT(List.of("dryout", "dry out")),
    ENTITY_ATTACK(List.of("entity attack", "attack")),
    ENTITY_EXPLOSION(List.of("entity explosion", "explosion")),
    FALL(List.of("fall", "falling")),
    FALLING_BLOCK(List.of("falling block")),
    FIRE(List.of("fire")),
    FIRE_TICK(List.of("burning", "burn")),
    FLY_INTO_WALL(List.of("fly into wall")),
    FREEZE(List.of("freeze", "freezing")),
    HOT_FLOOR(List.of("hot floor")),
    KILL(List.of("kill")),
    LAVA(List.of("lava")),
    LIGHTNING(List.of("lightning")),
    MAGIC(List.of("magic", "potion")),
    PROJECTILE(List.of("projectile")),
    SONIC_BOOM(List.of("sonic boom")),
    STARVATION(List.of("starvation", "hunger")),
    SUFFOCATION(List.of("suffocation", "suffocate")),
    THORNS(List.of("thorns")),
    VOID(List.of("void", "out of world")),
    WITHER(List.of("wither", "wither effect")),
    WORLD_BORDER(List.of("world border"));

    private static final Map<String, DamageCause> BY_DAMAGE_TYPE = Map.ofEntries(
            Map.entry("in_fire", FIRE),
            Map.entry("campfire", CAMPFIRE),
            Map.entry("lightning_bolt", LIGHTNING),
            Map.entry("on_fire", FIRE_TICK),
            Map.entry("lava", LAVA),
            Map.entry("hot_floor", HOT_FLOOR),
            Map.entry("sulfur_cube_hot", HOT_FLOOR),
            Map.entry("in_wall", SUFFOCATION),
            Map.entry("cramming", CRAMMING),
            Map.entry("drown", DROWNING),
            Map.entry("starve", STARVATION),
            Map.entry("cactus", CONTACT),
            Map.entry("sweet_berry_bush", CONTACT),
            Map.entry("fall", FALL),
            Map.entry("ender_pearl", FALL),
            Map.entry("stalagmite", FALL),
            Map.entry("fly_into_wall", FLY_INTO_WALL),
            Map.entry("fell_out_of_world", VOID),
            Map.entry("generic", CUSTOM),
            Map.entry("magic", MAGIC),
            Map.entry("indirect_magic", MAGIC),
            Map.entry("wither", WITHER),
            Map.entry("wither_skull", PROJECTILE),
            Map.entry("dragon_breath", DRAGON_BREATH),
            Map.entry("dry_out", DRYOUT),
            Map.entry("freeze", FREEZE),
            Map.entry("falling_block", FALLING_BLOCK),
            Map.entry("falling_anvil", FALLING_BLOCK),
            Map.entry("falling_stalactite", FALLING_BLOCK),
            Map.entry("sting", ENTITY_ATTACK),
            Map.entry("mob_attack", ENTITY_ATTACK),
            Map.entry("mob_attack_no_aggro", ENTITY_ATTACK),
            Map.entry("player_attack", ENTITY_ATTACK),
            Map.entry("spear", ENTITY_ATTACK),
            Map.entry("mace_smash", ENTITY_ATTACK),
            Map.entry("arrow", PROJECTILE),
            Map.entry("trident", PROJECTILE),
            Map.entry("mob_projectile", PROJECTILE),
            Map.entry("spit", PROJECTILE),
            Map.entry("wind_charge", PROJECTILE),
            Map.entry("fireball", PROJECTILE),
            Map.entry("unattributed_fireball", PROJECTILE),
            Map.entry("thrown", PROJECTILE),
            Map.entry("fireworks", ENTITY_EXPLOSION),
            Map.entry("explosion", ENTITY_EXPLOSION),
            Map.entry("player_explosion", ENTITY_EXPLOSION),
            Map.entry("bad_respawn_point", BLOCK_EXPLOSION),
            Map.entry("thorns", THORNS),
            Map.entry("sonic_boom", SONIC_BOOM),
            Map.entry("outside_border", WORLD_BORDER),
            Map.entry("generic_kill", KILL));

    private final List<String> names;

    DamageCause(List<String> names) {
        this.names = names;
    }

    public static Optional<DamageCause> parse(String text) {
        String name = text.strip().toLowerCase(Locale.ROOT);
        for (DamageCause cause : values()) {
            if (cause.names.contains(name)) {
                return Optional.of(cause);
            }
        }
        return Optional.empty();
    }

    /**
     * The cause Skript would report for a vanilla damage type, such as minecraft:mob_attack. Damage types this table
     * does not know, from a newer version or a data pack, are custom.
     */
    public static DamageCause fromDamageType(String id) {
        String path = id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
        return BY_DAMAGE_TYPE.getOrDefault(path, CUSTOM);
    }

    @Override
    public String toString() {
        return names.get(0);
    }
}
