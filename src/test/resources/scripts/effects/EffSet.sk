# Tests for EffSet: setting single variables, local variables and whole lists.

test "single variable":
	assert {x} is not set with "{x} was already set when the test started"
	set {x} to 5
	assert {x} is 5 with "set {x} to 5 did not store 5"
	set {x} to "hello"
	assert {x} is "hello" with "set did not replace a number with text"

test "local variable":
	set {_local} to 3
	assert {_local} is 3 with "set did not store a local variable"
	assert {local} is not set with "setting {_local} also set the global {local}"

test "list variable":
	set {list::*} to "a", "b" and "c"
	assert size of {list::*} is 3 with "setting a list of three values did not give three entries"
	assert {list::1} is "a" with "the first entry was not stored under index 1"
	assert {list::3} is "c" with "the third entry was not stored under index 3"
	set {list::*} to "z"
	assert size of {list::*} is 1 with "setting a list again did not replace the old entries"
	assert {list::1} is "z" with "setting a list again did not store the new value"

test "list entry":
	set {scores::alex} to 10
	assert {scores::alex} is 10 with "set did not store a named list entry"
	assert size of {scores::*} is 1 with "a named entry did not count in the list"
