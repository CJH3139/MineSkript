# Tests for the built-in text functions registered by TextFunctions: concat and formatNumber.

test "concat":
	assert concat("level ", 5, "!") is "level 5!" with "concat did not join the values"
	set {_parts::*} to "a", "b" and "c"
	assert concat({_parts::*}) is "abc" with "concat of a list was not abc"

test "format number":
	assert formatNumber(1234567) is "1,234,567" with "formatNumber(1234567) was %formatNumber(1234567)%"
	assert formatNumber(1234.567) is "1,234.57" with "formatNumber(1234.567) was %formatNumber(1234.567)%"
	assert formatNumber(3.14159, "0.0") is "3.1" with "formatNumber with a format was wrong"
	assert formatNumber(1, "0.0.0") is not set with "an invalid format gave a value"
