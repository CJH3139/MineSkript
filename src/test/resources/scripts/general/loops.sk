# Loop sections: loop N times, loop over lists, while, and nested loops.

test "loop times":
	loop 3 times:
		add 1 to {_count}
	assert {_count} is 3 with "loop 3 times did not run three times"
	loop 0 times:
		assert false with "loop 0 times ran"
	loop twice:
		add 1 to {_twice}
	assert {_twice} is 2 with "loop twice did not run twice"

test "while":
	set {_n} to 1
	while {_n} is less than 100:
		set {_n} to {_n} * 2
	assert {_n} is 128 with "while did not stop at the first power of two over 100"

test "nested loops":
	loop 3 times:
		loop 4 times:
			add 1 to {_count}
	assert {_count} is 12 with "a loop inside a loop did not run 3 times 4 times"

test "loop over split text":
	loop "a b c" split at " ":
		set {_last} to loop-value
	assert {_last} is "c" with "looping split text did not end on the last piece"
