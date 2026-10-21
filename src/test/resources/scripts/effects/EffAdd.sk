# Tests for EffAdd: adding to numbers and to lists.

test "add to a number":
	set {n} to 5
	add 4 to {n}
	assert {n} is 9 with "add 4 to 5 did not give 9"

test "add to an unset variable":
	add 2 to {fresh}
	assert {fresh} is 2 with "adding to an unset variable did not start from 0"

test "add to a list":
	add "a" to {list::*}
	add "b" to {list::*}
	assert size of {list::*} is 2 with "adding twice to a list did not give two entries"
	assert {list::1} is "a" with "the first added value is not at index 1"
	assert {list::2} is "b" with "the second added value is not at index 2"

test "add text to a number fails":
	set {n} to 1
	try:
		add "x" to {n}
	on error:
		set {_failed} to true
	assert {_failed} is true with "adding text to a number did not fail"
