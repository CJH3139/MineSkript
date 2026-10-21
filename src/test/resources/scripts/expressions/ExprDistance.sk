# Tests for ExprDistance: the distance between two locations.

test "distance":
	assert distance between location(0, 64, 0) and location(3, 68, 0) is 5 with "the distance was not 5"
	assert distance between player and location(0.5, 64, 0.5) is 4 with "the distance to the player was not 4"

test "no distance across dimensions":
	set {_d} to distance between location(0, 0, 0) and location(0, 0, 0, "the_nether")
	assert {_d} is not set with "a distance across dimensions had a value"
