# Tests for ExprRelativeLocation: locations above, below and to the side of another one, in the
# Skript form (the location 2 above) and the short form (2 above).

test "the location form":
	set {_o} to location(0, 64, 0)
	assert the location 2 above {_o} is location(0, 66, 0) with "2 above was wrong"
	assert location north of {_o} is location(0, 64, -1) with "north was not -z"
	assert the position 3 meters east of {_o} is location(3, 64, 0) with "east was not +x"
	assert location 0.5 blocks under {_o} is location(0, 63.5, 0) with "under was wrong"

test "the short form":
	assert 2 below player is location(0.5, 62, -3.5) with "2 below player was wrong"
	assert 3 blocks south of player is location(0.5, 64, -0.5) with "south was not +z"
	assert 1.5 west of player is location(-1, 64, -3.5) with "west was not -x"

test "the dimension is kept":
	set {_n} to location(0, 64, 0, "the_nether")
	assert 1 above {_n} is location(0, 65, 0, "the_nether") with "the dimension changed"
