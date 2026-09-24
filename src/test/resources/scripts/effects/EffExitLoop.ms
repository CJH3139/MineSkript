# Tests for EffExitLoop.

test "exit loop stops only the loop":
	loop 10 times:
		add 1 to {_count}
		if loop-iteration is 3:
			exit loop
	assert {_count} is 3 with "exit loop did not stop the loop on the third pass"
	set {_after} to true
	assert {_after} is true with "the lines after the loop did not run"

test "exit loop in a while loop":
	set {_n} to 0
	while {_n} is less than 100:
		add 1 to {_n}
		if {_n} is 7:
			exit loop
	assert {_n} is 7 with "exit loop did not stop a while loop"
