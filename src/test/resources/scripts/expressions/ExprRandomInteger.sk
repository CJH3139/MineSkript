# Tests for ExprRandomInteger: the result is always a whole number inside the range.

test "stays in range":
	loop 200 times:
		set {_roll} to random integer between 1 and 6
		assert {_roll} is between 1 and 6 with "a roll between 1 and 6 was %{_roll}%"
		assert {_roll} is floor {_roll} with "a roll was not a whole number: %{_roll}%"

test "a range of one number":
	loop 20 times:
		assert random integer between 4 and 4 is 4 with "a random integer between 4 and 4 was not 4"

test "every value can come up":
	loop 300 times:
		set {_seen::%random integer between 1 and 3%} to true
	assert size of {_seen::*} is 3 with "300 rolls between 1 and 3 did not produce all three values"
