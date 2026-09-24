# The test event itself: named and unnamed tests both run.

test:
	set {_ran} to true
	assert {_ran} is true with "an unnamed test did not run"

test "named":
	assert {_ran} is not set with "a local variable leaked between tests"

test "assert with the default message":
	assert 1 is 1
