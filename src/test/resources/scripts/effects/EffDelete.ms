# Tests for EffDelete: deleting single variables, list entries and whole lists.

test "delete a variable":
	set {x} to 5
	delete {x}
	assert {x} is not set with "delete did not unset {x}"

test "clear a whole list":
	set {list::*} to 1, 2 and 3
	clear {list::*}
	assert size of {list::*} is 0 with "clear did not empty the list"
	assert {list::*} is not set with "an emptied list still counts as set"

test "delete one entry":
	set {list::*} to 1, 2 and 3
	delete {list::2}
	assert size of {list::*} is 2 with "deleting one entry did not leave two"
	assert {list::3} is 3 with "deleting one entry moved the others"

test "delete nested entries":
	set {tree::a::b} to 1
	set {tree::c} to 2
	delete {tree::*}
	assert {tree::a::b} is not set with "deleting a list did not delete its nested entries"
