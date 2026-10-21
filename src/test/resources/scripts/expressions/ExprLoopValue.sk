# Tests for ExprLoopValue and ExprLoopIteration.

test "loop over a written list":
	set {_seen} to ""
	loop "red", "green" and "blue":
		set {_seen} to "%{_seen}%%loop-value%"
	assert {_seen} is "redgreenblue" with "the loop did not visit every value in order: %{_seen}%"

test "loop over a list variable":
	set {_l::*} to 2, 4 and 6
	loop {_l::*}:
		add loop-value to {_sum}
	assert {_sum} is 12 with "the values of the list did not add up to 12"

test "loop iteration":
	loop 4 times:
		add loop-iteration to {_sum}
	assert {_sum} is 10 with "loop-iteration did not count 1 to 4"

test "loop-value in loop N times":
	loop 3 times:
		set {_last} to loop-value
	assert {_last} is 3 with "loop-value in loop 3 times did not end at 3"
