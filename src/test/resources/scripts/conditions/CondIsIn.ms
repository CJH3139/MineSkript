# Tests for CondIsIn and CondListContains.

test "is in a written list":
	assert "b" is in "a", "b" and "c" with "b was not found in a, b and c"
	assert "d" is not in "a", "b" and "c" with "d was found in a, b and c"

test "is in a list variable":
	set {_friends::*} to "Alex" and "Bea"
	assert "Alex" is in {_friends::*} with "Alex was not in the list variable"
	assert "alex" is in {_friends::*} with "is in did not ignore capitals"
	assert "cy" is not in {_friends::*} with "is in found an entry that is not there"
	assert {_friends::*} contains "BEA" with "list contains did not ignore capitals"
	assert {_friends::*} contains "Bea" with "contains did not find Bea"
	assert {_friends::*} doesn't contain "Cy" with "contains found Cy"

test "an empty list contains nothing":
	assert "a" is not in {_empty::*} with "an empty list contained a value"
