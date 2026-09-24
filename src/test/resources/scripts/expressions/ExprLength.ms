# Tests for ExprLength.

test "length of text":
	assert length of "hello" is 5 with "the length of hello was not 5"
	assert length of "" is 0 with "the length of empty text was not 0"
	set {_t} to "abc"
	assert {_t}'s length is 3 with "the possessive spelling did not work"

test "length counts an escaped quote once":
	assert length of "say ""hi""" is 8 with "a doubled quote inside text counted as two characters"
