# Tests for EffWait: waits pause the test and simulated time moves on.

test "wait lets time pass":
	set {_before} to game time
	wait 5 ticks
	assert game time - {_before} is 5 with "wait 5 ticks did not let 5 ticks pass"
	wait 1 second
	assert game time - {_before} is 25 with "wait 1 second did not let 20 more ticks pass"

test "wait inside a loop":
	set {_before} to game time
	loop 3 times:
		wait 2 ticks
	assert game time - {_before} is 6 with "three waits of 2 ticks did not add up to 6"
