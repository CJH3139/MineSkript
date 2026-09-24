# Options are replaced into the script text before it is parsed.

options:
	greeting: hello there
	amount: 3
	variable: {opt::value}

test "options":
	assert "{@greeting}" is "hello there" with "an option inside text was not replaced"
	loop {@amount} times:
		add 1 to {_count}
	assert {_count} is 3 with "an option used as a number was not replaced"
	set {@variable} to 5
	assert {opt::value} is 5 with "an option naming a variable did not reach that variable"
