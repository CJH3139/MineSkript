# Tests for EffContinue.

test "continue skips the rest of a pass":
	loop 5 times:
		if loop-iteration is 3:
			continue
		add loop-iteration to {_sum}
	assert {_sum} is 12 with "continue did not skip only the third pass (1 + 2 + 4 + 5 is 12)"
