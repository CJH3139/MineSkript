# Tests for ExprReplace. It ignores capitals unless told to use case sensitivity, like Skript's replace.

local function exactly(a: text, b: text) :: boolean:
	if length of {_a} is not length of {_b}:
		return false
	if ({_a} with {_b} replaced with "" with case sensitivity) is "":
		return true
	return false

test "replace every occurrence":
	set {_t} to "gg and gg" with "gg" replaced with "good game"
	assert {_t} is "good game and good game" with "not every gg was replaced"

test "replace ignores capitals":
	set {_t} to "GG gg" with "gg" replaced with "x"
	assert exactly({_t}, "x x") with "replace did not ignore capitals, gave %{_t}%"

test "replace with case sensitivity":
	set {_t} to "GG gg" with "gg" replaced with "x" with case sensitivity
	assert exactly({_t}, "GG x") with "replace with case sensitivity changed text with different capitals"

test "replace with nothing":
	set {_t} to "a-b-c" with "-" replaced with ""
	assert {_t} is "abc" with "replacing with empty text did not remove the dashes"

test "special characters are literal":
	set {_t} to "a.b-c" with "-" replaced with "$0"
	assert {_t} is "a.b$0c" with "a dollar sign in the replacement was not treated literally"
	set {_t} to "a.b.c" with "." replaced with "+"
	assert {_t} is "a+b+c" with "a dot was not searched for literally"
