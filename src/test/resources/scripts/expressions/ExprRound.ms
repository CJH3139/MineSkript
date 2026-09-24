# Tests for ExprRound, ExprCeiling and ExprRoundedToPlaces.

test "round":
	assert round 2.4 is 2 with "round 2.4 was not 2"
	assert round 2.6 is 3 with "round 2.6 was not 3"
	assert round -2.6 is -3 with "round -2.6 was not -3"

test "ceiling":
	assert ceil 2.1 is 3 with "ceil 2.1 was not 3"
	assert ceiling 2 is 2 with "ceiling of a whole number changed it"

test "rounded to places":
	assert (3.14159 rounded to 2 places) is 3.14 with "pi rounded to 2 places was not 3.14"
