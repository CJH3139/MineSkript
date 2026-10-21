# Tests for CondIsSet.

test "variables":
	assert {x} is not set with "a variable that was never set counts as set"
	set {x} to 0
	assert {x} is set with "a variable set to 0 does not count as set"
	delete {x}
	assert {x} isn't set with "a deleted variable still counts as set"

test "lists":
	assert {l::*} is not set with "an empty list counts as set"
	add 1 to {l::*}
	assert {l::*} is set with "a list with one entry does not count as set"

test "several values":
	set {a} to 1
	assert {a} and {b} are not set with "two values with one unset counted as set"
	set {b} to 2
	assert {a} and {b} are set with "two set values did not count as set"
