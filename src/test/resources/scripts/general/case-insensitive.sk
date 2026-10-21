# Text comparison ignores capitals everywhere, like Skript's default case sensitive: false, and the ternary accepts
# Skript's otherwise and comma.

test "is and is not":
	assert "Hello" is "hello" with "is did not ignore capitals"
	assert "HELLO" = "hello" with "= did not ignore capitals"
	assert "Hello" is not "world" with "is not matched different text"
	assert "Hello" is "hello" ignoring case with "ignoring case stopped working"
	set {_m} to "AFK"
	assert {_m} is "afk" with "a variable holding text did not ignore capitals"
	assert {_m} is "x", "y" or "Afk" with "an or list did not ignore capitals"

test "lists":
	set {_l::*} to "Red", "Green" and "Blue"
	assert "green" is in {_l::*} with "is in did not ignore capitals"
	assert {_l::*} contains "BLUE" with "list contains did not ignore capitals"
	remove "red" from {_l::*}
	assert size of {_l::*} is 2 with "remove did not ignore capitals"

test "text checks":
	assert "Minecraft" contains "CRAFT" with "contains did not ignore capitals"
	assert "Minecraft" starts with "mine" with "starts with did not ignore capitals"
	assert "Minecraft" ends with "CRAFT" with "ends with did not ignore capitals"

test "ternary with otherwise and a comma":
	set {_n} to 2
	assert ("one" if {_n} is 1 otherwise "other") is "other" with "otherwise did not work"
	assert ("two" if {_n} is 2, else "other") is "two" with "a comma before else did not work"
	assert ("one" if {_n} is 1, otherwise "other") is "other" with "a comma before otherwise did not work"
	assert ("a" if {_n} is 1 else "b") is "b" with "plain else stopped working"
