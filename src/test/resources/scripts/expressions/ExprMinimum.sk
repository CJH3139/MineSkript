# Tests for ExprMinimum and ExprMaximum.

test "minimum":
	assert min of 3 and 7 is 3 with "min of 3 and 7 was not 3"
	assert the minimum of -1 and 0 is -1 with "the minimum of -1 and 0 was not -1"

test "maximum":
	assert max of 3 and 7 is 7 with "max of 3 and 7 was not 7"
	set {_over} to 10 - 64
	assert max of {_over} and 0 is 0 with "max of a negative number and 0 was not 0"
