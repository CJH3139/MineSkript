# Tests for ExprJoin.

test "join two texts":
	set {_a} to "Mine"
	set {_b} to "Skript"
	assert ({_a} joined with {_b}) is "MineSkript" with "joining Mine and Skript did not give MineSkript"

test "join with literals":
	assert ("a" joined with "") is "a" with "joining with empty text changed the text"
