# Variable names: dynamic names, list variables, case and scope.

test "dynamic names":
	set {_name} to "alex"
	set {score::%{_name}%} to 3
	assert {score::alex} is 3 with "a name built from a variable did not reach the same variable"
	loop "a", "b" and "c":
		set {letter::%loop-value%} to loop-iteration
	assert {letter::b} is 2 with "a name built from loop-value did not work"

test "names ignore case":
	set {Mixed Case} to 1
	assert {mixed case} is 1 with "variable names were case sensitive"

test "list entries are variables":
	set {l::*} to "x" and "y"
	set {l::2} to "z"
	assert {l::*} contains "z" with "changing one entry did not change the list"
	assert size of {l::*} is 2 with "changing one entry changed the size"

test "a list inside text":
	set {_l::*} to "a", "b" and "c"
	assert "%{_l::*}%" is "a, b and c" with "a list in text did not read a, b and c"

test "locals are fresh in each test":
	assert {_name} is not set with "a local variable leaked from another test"
	assert {score::alex} is not set with "a global variable leaked from another test"
