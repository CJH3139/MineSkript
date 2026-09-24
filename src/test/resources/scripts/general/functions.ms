# Functions: return values, default parameters, recursion and functions that do not return.

function double(n: number) :: number:
	return {_n} * 2

function greet(name: text, greeting: text = "hello") :: text:
	return "%{_greeting}% %{_name}%"

function factorial(n: number) :: number:
	if {_n} <= 1:
		return 1
	return {_n} * factorial({_n} - 1)

function remember(value: number):
	set {remembered} to {_value}

test "return a value":
	assert double(4) is 8 with "double(4) was not 8"
	assert double(double(1)) is 4 with "a call inside a call did not work"

test "default parameters":
	assert greet("alex") is "hello alex" with "the default greeting was not used"
	assert greet("alex", "hi") is "hi alex" with "a given greeting did not replace the default"

test "recursion":
	assert factorial(5) is 120 with "factorial(5) was not 120"

test "a function on its own line":
	remember(7)
	assert {remembered} is 7 with "a function called on its own line did not run"

test "locals do not leak":
	set {_n} to 100
	set {_d} to double(3)
	assert {_n} is 100 with "a function changed the local variables of its caller"
