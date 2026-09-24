# Tests for ExprSubstring: the part of a text between two positions, and the first or last characters.

test "substring":
	set {_t} to "minecraft"
	assert ({_t} from character 5 to 9) is "craft" with "characters 5 to 9 of minecraft were not craft"
	assert ({_t} from character 1 to 4) is "mine" with "characters 1 to 4 of minecraft were not mine"

test "part of and subtext of":
	set {_t} to "abcdef"
	assert (part of {_t} between 2 and 4) is "bcd" with "part of between 2 and 4 was not bcd"
	assert (the subtext of {_t} from characters 2 to 4) is "bcd" with "subtext of from characters was not bcd"
	assert (substring of {_t} from index 1 to index 3) is "abc" with "substring from index to index was not abc"
	assert (sub text of {_t} between indices 5 and character 6) is "ef" with "sub text between indices was not ef"
	assert (part of {_t} from 4 to 99) is "def" with "a range past the end was not clipped"
	assert (part of {_t} from 5 to 2) is "" with "a backwards range was not empty"

test "first and last characters":
	assert (first 4 characters of "minecraft") is "mine" with "the first 4 characters were not mine"
	assert (last 5 characters of "minecraft") is "craft" with "the last 5 characters were not craft"
	assert (the 4 first characters of "minecraft") is "mine" with "the 4 first characters were not mine"
	assert (the 5 last characters of "minecraft") is "craft" with "the 5 last characters were not craft"
	assert (first character of "minecraft") is "m" with "the first character was not m"
	assert (the last character of "minecraft") is "t" with "the last character was not t"
	assert (first 20 characters of "abc") is "abc" with "a count past the end did not give the whole text"
	assert (last 0 characters of "abc") is "" with "a count of 0 did not give empty text"

test "a list of texts":
	set {_words::*} to "alpha" and "beta"
	set {_starts::*} to first 2 characters of {_words::*}
	assert size of {_starts::*} is 2 with "a list of texts did not give one part each"
	assert {_starts::2} is "be" with "the second part was not be"
