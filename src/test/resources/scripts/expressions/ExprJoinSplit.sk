# Tests for ExprJoinSplit: join and concat with a delimiter, and split, which ignores capitals unless told otherwise.

local function exactly(a: text, b: text) :: boolean:
	if length of {_a} is not length of {_b}:
		return false
	if ({_a} with {_b} replaced with "" with case sensitivity) is "":
		return true
	return false

test "split into a list":
	set {_parts::*} to "a,b,c" split at ","
	assert size of {_parts::*} is 3 with "a,b,c split at a comma did not give three pieces"
	assert {_parts::2} is "b" with "the second piece was not b"

test "split spellings":
	set {_parts::*} to split "x y z" by " "
	assert size of {_parts::*} is 3 with "split X by Y did not work"
	set {_parts::*} to "1-2" split on "-"
	assert {_parts::1} is "1" with "split on did not work"
	set {_parts::*} to split "1;2;3" using ";"
	assert size of {_parts::*} is 3 with "split X using Y did not work"
	set {_parts::*} to split "1;2" at the delimiter ";"
	assert size of {_parts::*} is 2 with "split X at the delimiter Y did not work"
	set {_parts::*} to "1;2" split using delimiter ";"
	assert size of {_parts::*} is 2 with "X split using delimiter Y did not work"

test "empty pieces are kept":
	set {_parts::*} to "a,,b" split at ","
	assert size of {_parts::*} is 3 with "an empty piece between two commas was dropped"
	assert {_parts::2} is "" with "the empty piece was not empty text"
	set {_parts::*} to "a,b,," split at ","
	assert size of {_parts::*} is 4 with "trailing empty pieces were dropped without being asked"

test "without trailing text":
	set {_parts::*} to split "a,b,," at "," without trailing text
	assert size of {_parts::*} is 2 with "without trailing text kept the empty pieces at the end"
	set {_parts::*} to "a,b," split at "," without the trailing empty string
	assert size of {_parts::*} is 2 with "without the trailing empty string kept the empty piece"

test "empty separator splits into characters":
	set {_parts::*} to "abc" split at ""
	assert size of {_parts::*} is 3 with "an empty separator did not split into single characters"
	assert {_parts::3} is "c" with "the last character was not c"

test "split ignores capitals":
	set {_parts::*} to "aXbxc" split at "x"
	assert size of {_parts::*} is 3 with "split did not ignore capitals"
	assert exactly({_parts::2}, "b") with "the middle piece was not b"

test "split with case sensitivity":
	set {_parts::*} to split "aXbxc" at "x" with case sensitivity
	assert size of {_parts::*} is 2 with "split with case sensitivity split at a capital"
	assert exactly({_parts::1}, "aXb") with "the first piece was not aXb"
	set {_parts::*} to "aXbxc" split at "X" with case sensitivity
	assert exactly({_parts::2}, "bxc") with "X split at Y with case sensitivity did not split at the capital"

test "join":
	set {_names::*} to "a", "b" and "c"
	assert (join {_names::*} with ", ") is "a, b, c" with "join with a delimiter did not work"
	assert (join {_names::*}) is "abc" with "join without a delimiter did not join with nothing"
	assert (concat {_names::*} using "-") is "a-b-c" with "concat using did not work"
	assert (concatenate "x", "y" by the delimiter "+") is "x+y" with "concatenate by the delimiter did not work"
	assert (join "only") is "only" with "joining one text changed it"
	assert (join {_empty::*} with ",") is "" with "joining an empty list was not empty text"

test "join does not clash with the concat function":
	assert concat("a", "b") is "ab" with "the concat function stopped working"
	assert (concat "a", "b" and "c" by "") is "abc" with "the concat expression did not work"
	set {_l::*} to "x" and "y"
	assert (concat {_l::*}) is "xy" with "concat of a list variable did not work"
