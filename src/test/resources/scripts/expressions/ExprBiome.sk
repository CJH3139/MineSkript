# Tests for ExprBiome with a location. Also covers the location forms of ExprLightLevel, ExprSkyLight,
# ExprDimension (world of) and ExprBlock (block above a location). The fake world is plains, fully lit
# and made of air.

test "values at a location":
	set {_spot} to location(10, 64, 10)
	assert biome of {_spot} is "minecraft:plains" with "biome of a location was not plains"
	assert {_spot}'s biome is biome with "the possessive biome differed"
	assert light level of {_spot} is 15 with "light level of a location was not 15"
	assert block light level of {_spot} is 15 with "block light level was not 15"
	assert sky light level of {_spot} is 15 with "sky light level was not 15"
	assert sunlight level of player is 15 with "sunlight level of player was not 15"
	assert block above {_spot} is air with "block above a location was not air"
	assert block 2 below {_spot} is air with "block 2 below a location was not air"

test "the world of a location":
	assert world of player is "minecraft:overworld" with "world of player was not the overworld"
	assert player's world is dimension with "player's world was not the dimension"
	assert world is dimension with "world was not the dimension"
	assert world of location(0, 0, 0, "the_end") is "minecraft:the_end" with "world of a location was wrong"

test "another dimension has no biome, light or block":
	set {_end} to location(0, 64, 0, "the_end")
	assert biome of {_end} is not set with "a location in the end had a biome"
	assert light level of {_end} is not set with "a location in the end had a light level"
	assert block north of {_end} is not set with "a location in the end had a block"
