# Arithmetic and parsing numbers from text.

test "operators":
	assert 1 + 2 * 3 is 7 with "multiplication did not come before addition"
	assert (1 + 2) * 3 is 9 with "brackets did not group"
	assert 10 / 4 is 2.5 with "division did not give a decimal"
	assert 2 ^ 10 is 1024 with "powers did not work"
	assert 7 - 10 is -3 with "subtraction below zero did not work"

test "division by zero is an error":
	try:
		set {_x} to 1 / 0
	on error:
		set {_message} to {_error}
	assert {_message} is "division by zero" with "dividing by zero gave %{_message}%"

test "parsed as number":
	assert ("41" parsed as number) + 1 is 42 with "41 parsed as a number was not 41"
	assert "abc" parsed as number is not set with "text that is not a number parsed as one"

test "square root":
	assert sqrt of 16 is 4 with "sqrt of 16 was not 4"
