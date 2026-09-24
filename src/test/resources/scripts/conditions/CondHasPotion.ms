# Tests for CondHasPotion, Skript's has potion. Also covers its relatives CondIsPoisoned and
# ExprPotionEffectTier. The fake player has no effects.

test "no potion effects":
	assert player doesn't have any potion effects with "the player had a potion effect"
	assert player does not have potion speed with "the player had speed"
	assert player don't have potion effects night vision and fire resistance with "the player had night vision"
	assert player doesn't have potion swiftness or fast digging with "Skript's other effect names were active"

test "not poisoned":
	assert player is not poisoned with "the player was poisoned"
	assert me isn't poisoned with "me was poisoned"

test "the tier of an effect that is not active is 0":
	assert tier of speed of player is 0 with "an inactive effect had a tier"
	assert the potion amplifier of jump boost for me is 0 with "amplifier of jump boost was not 0"
	assert level of minecraft:night_vision on player is 0 with "an effect id had a tier"
