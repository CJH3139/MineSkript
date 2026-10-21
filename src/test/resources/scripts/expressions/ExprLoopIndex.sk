# Tests for ExprLoopIndex.

test "index of a named list":
	set {_homes::alex} to 1
	set {_homes::bea} to 2
	loop {_homes::*}:
		if loop-value is 2:
			set {_found} to loop-index
	assert {_found} is "bea" with "loop-index was not the index of the entry"

test "index of a numbered list":
	loop "a", "b" and "c":
		if loop-value is "b":
			set {_found} to loop-index
	assert {_found} is "2" with "loop-index of the second entry was not 2"
