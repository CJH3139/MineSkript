# Tests for ExprSubstring and the first and last characters expressions.

test "substring":
	set {_t} to "minecraft"
	assert ({_t} from character 5 to 9) is "craft" with "characters 5 to 9 of minecraft were not craft"
	assert ({_t} from character 1 to 4) is "mine" with "characters 1 to 4 of minecraft were not mine"

test "first and last characters":
	assert (first 4 characters of "minecraft") is "mine" with "the first 4 characters were not mine"
	assert (last 5 characters of "minecraft") is "craft" with "the last 5 characters were not craft"
