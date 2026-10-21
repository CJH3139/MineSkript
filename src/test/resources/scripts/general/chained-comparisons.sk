# Comparisons written as a chain, such as 1 < 2 < 3, and conditions joined with and, or and not.

test "chains":
	set {_x} to 5
	assert 1 < {_x} < 10 with "1 < 5 < 10 failed"
	assert 0 <= {_x} <= 5 with "0 <= 5 <= 5 failed"
	if 1 < {_x} < 3:
		assert false with "1 < 5 < 3 passed"

test "and, or and not":
	set {_x} to 5
	assert {_x} is 5 and {_x} > 1 with "two true conditions joined with and failed"
	assert {_x} is 4 or {_x} is 5 with "or did not pass when the second condition was true"
	assert not {_x} is 4 with "not did not flip a false condition"
	if {_x} is 4 and {_x} is 5:
		assert false with "and passed with a false condition"
	if not {_x} is 5:
		assert false with "not did not flip a true condition"

test "brackets":
	set {_x} to 5
	assert ({_x} is 1 or {_x} is 5) and {_x} > 0 with "brackets around an or did not group it"
