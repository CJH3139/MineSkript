# Tests for ExprSplit.

test "split into a list":
	set {_parts::*} to "a,b,c" split at ","
	assert size of {_parts::*} is 3 with "a,b,c split at a comma did not give three pieces"
	assert {_parts::2} is "b" with "the second piece was not b"

test "split spellings":
	set {_parts::*} to split "x y z" by " "
	assert size of {_parts::*} is 3 with "split X by Y did not work"
	set {_parts::*} to "1-2" split on "-"
	assert {_parts::1} is "1" with "split on did not work"

test "empty pieces are kept":
	set {_parts::*} to "a,,b" split at ","
	assert size of {_parts::*} is 3 with "an empty piece between two commas was dropped"
	assert {_parts::2} is "" with "the empty piece was not empty text"

test "empty separator splits into characters":
	set {_parts::*} to "abc" split at ""
	assert size of {_parts::*} is 3 with "an empty separator did not split into single characters"
	assert {_parts::3} is "c" with "the last character was not c"
