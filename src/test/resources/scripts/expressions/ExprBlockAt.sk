# Tests for ExprBlockAt: the block at a location. The fake world is air everywhere.

test "block at a location":
	assert block at location(10, 64, 10) is air with "an empty block was not air"
	assert block at 2 above player is air with "block at a relative location did not parse"

test "no block in another dimension":
	set {_b} to block at location(10, 64, 10, "the_nether")
	assert {_b} is not set with "a block in another dimension had a value"
