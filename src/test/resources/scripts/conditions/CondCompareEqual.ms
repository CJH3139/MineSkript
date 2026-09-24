# Tests for CondCompareEqual and the other comparisons.

test "equal":
	assert 1 is 1 with "1 is not 1"
	assert 1 = 1 with "1 = 1 failed"
	assert "a" is "a" with "text was not equal to itself"
	assert "a" is "A" with "text comparison did not ignore capitals"
	assert "a" is not "b" with "different text was equal"
	assert 2 != 3 with "2 != 3 failed"

test "or and and lists":
	assert 2 is 1, 2 or 3 with "2 did not match a list joined with or"
	assert 4 is not 1, 2 or 3 with "4 matched a list joined with or"

test "unset values":
	assert {nothing} is not 5 with "an unset variable was equal to 5"

test "ordering":
	assert 3 is greater than 2 with "3 is greater than 2 failed"
	assert 2 is less than 3 with "2 is less than 3 failed"
	assert 3 > 2 with "3 > 2 failed"
	assert 2 < 3 with "2 < 3 failed"
	assert 3 >= 3 with "3 >= 3 failed"
	assert 3 <= 3 with "3 <= 3 failed"
	assert 5 is between 1 and 10 with "5 was not between 1 and 10"
	assert 11 is not between 1 and 10 with "11 was between 1 and 10"
