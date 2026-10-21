# Tests for ExprLocationCoordinate, and that the player coordinates keep working next to it.

test "coordinates of a location":
	set {_l} to location(10, 64.5, -3)
	assert x-coordinate of {_l} is 10 with "x was not 10"
	assert {_l}'s y-coord is 64.5 with "y was not 64.5"
	assert z coordinate of {_l} is -3 with "z was not -3"
	assert x-coordinate of location(7, 8, 9) is 7 with "x of a location function was not 7"

test "coordinates of the player and relative locations":
	assert player's x-coordinate is 0.5 with "the player's x-coordinate changed"
	assert y-coordinate of 2 above player is 66 with "y of 2 above player was not 66"
	assert x-coordinate of block below player is 0 with "the block below was not at x 0"
