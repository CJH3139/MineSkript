# Tests for ExprLocationOf: the location of the player, an entity and a block. The fake player is at
# 0.5, 64, -3.5 in the overworld.

test "location of the player":
	assert location of player is location(0.5, 64, -3.5) with "the player's location was %location of player%"
	assert player's position is location(0.5, 64, -3.5) with "player's position was wrong"
	assert location of me is location of myself with "me and myself were in different places"

test "location of a block is its corner":
	assert location of block below player is location(0, 63, -4) with "the block below was at %location of block below player%"
	assert location of block at location(10.7, 64, -3.2) is location(10, 64, -4) with "block at did not give the corner"

test "location of a location is itself":
	set {_l} to location(1, 2, 3)
	assert location of {_l} is {_l} with "the location of a location changed it"
