# Tests for EffRemove: removing from numbers and lists.

test "remove from a number":
	set {n} to 5
	remove 20 from {n}
	assert {n} is -15 with "remove 20 from 5 did not give -15"

test "remove one entry from a list":
	set {list::*} to "a", "b", "a" and "c"
	remove "a" from {list::*}
	assert size of {list::*} is 3 with "remove took away more than the first matching entry"
	assert {list::1} is not set with "the first matching entry was not the one removed"
	assert {list::3} is "a" with "the second matching entry was removed too"
	assert {list::4} is "c" with "the other entries did not keep their indices"

test "remove all from a list":
	set {list::*} to "a", "b", "a" and "c"
	remove all "a" from {list::*}
	assert size of {list::*} is 2 with "remove all left a matching entry behind"
	assert "a" is not in {list::*} with "remove all did not remove every a"
