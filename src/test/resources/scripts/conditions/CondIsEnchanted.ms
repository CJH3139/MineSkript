# Tests for CondIsEnchanted and the enchantment types. Also covers ExprEnchantmentLevel and
# ExprItemEnchantments. The fake hand is empty, so it holds air without enchantments.

test "air is not enchanted":
	assert held item is not enchanted with "air was enchanted"
	assert held item isn't enchanted with sharpness with "air was enchanted with sharpness"
	assert tool of player is not enchanted with efficiency 4 or better with "air had efficiency 4 or better"
	assert held item is not enchanted with sharpness or smite with "air had sharpness or smite"

test "enchantment levels of an item without enchantments":
	assert level of sharpness of held item is 0 with "level of sharpness was not 0"
	assert held item's sharpness level is 0 with "the possessive form was not 0"
	assert fire aspect enchantment level on held item is 0 with "the enchantment level form was not 0"
	assert held item's enchantment levels of luck of the sea is 0 with "luck of the sea was not 0"
	assert level of enchantment "sharpness" on held item is 0 with "the text form was not 0"
	assert size of enchantments of held item is 0 with "air had enchantments"

test "enchantment types compare with text":
	set {_type::*} to enchantments of held item
	assert {_type::*} is not set with "air gave enchantment types"
