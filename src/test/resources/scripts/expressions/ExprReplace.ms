# Tests for ExprReplace.

test "replace every occurrence":
	set {_t} to "gg and gg" with "gg" replaced with "good game"
	assert {_t} is "good game and good game" with "not every gg was replaced"

test "replace is case sensitive":
	set {_t} to "GG gg" with "gg" replaced with "x"
	assert {_t} is "GG x" with "replace changed text with different capitals"

test "replace with nothing":
	set {_t} to "a-b-c" with "-" replaced with ""
	assert {_t} is "abc" with "replacing with empty text did not remove the dashes"
