# Tests for the built-in maths functions registered by MathFunctions: rounding, mod, logarithms,
# trigonometry, lists of numbers and counting.

test "rounding":
	assert floor(2.99) is 2 with "floor(2.99) was not 2"
	assert floor(-2.1) is -3 with "floor(-2.1) was not -3"
	assert ceil(2.01) is 3 with "ceil(2.01) was not 3"
	assert ceiling(-2.5) is -2 with "ceiling(-2.5) was not -2"
	assert round(2.5) is 3 with "round(2.5) was not 3"
	assert round(-2.5) is -2 with "round(-2.5) was not -2"
	assert round(3.14159, 2) is 3.14 with "round(3.14159, 2) was not 3.14"
	assert round(1234, -2) is 1200 with "round(1234, -2) was not 1200"
	assert abs(-4) is 4 with "abs(-4) was not 4"

test "mod keeps the sign of the divisor":
	assert mod(3, 2) is 1 with "mod(3, 2) was not 1"
	assert mod(-1, 10) is 9 with "mod(-1, 10) was %mod(-1, 10)%"
	assert mod(1, -10) is -9 with "mod(1, -10) was %mod(1, -10)%"
	assert isNaN(mod(5, 0)) with "mod(5, 0) was a number"

test "powers and roots":
	assert exp(0) is 1 with "exp(0) was not 1"
	assert ln(1) is 0 with "ln(1) was not 0"
	assert round(log(100), 6) is 2 with "log(100) was not 2"
	assert round(log(16, 2), 6) is 4 with "log(16, 2) was not 4"
	assert sqrt(9) is 3 with "sqrt(9) was not 3"
	assert round(root(3, 27), 6) is 3 with "root(3, 27) was not 3"
	assert root(0, 5) is not set with "root(0, 5) had a value"
	assert isNaN(sqrt(-1)) with "sqrt(-1) was a number"
	assert isnan(1) is false with "isnan(1) was true"

test "trigonometry in degrees":
	assert round(sin(90), 6) is 1 with "sin(90) was not 1"
	assert round(cos(180), 6) is -1 with "cos(180) was not -1"
	assert round(tan(45), 6) is 1 with "tan(45) was not 1"
	assert round(asin(1), 6) is 90 with "asin(1) was not 90"
	assert round(acos(1), 6) is 0 with "acos(1) was not 0"
	assert round(atan(1), 6) is 45 with "atan(1) was not 45"
	assert round(atan2(0, 10), 6) is 90 with "atan2(0, 10) was not 90"

test "lists of numbers":
	set {_l::*} to 4, 9 and -2
	assert max(1, 5, 3) is 5 with "max(1, 5, 3) was not 5"
	assert min(1, 5, 3) is 1 with "min(1, 5, 3) was not 1"
	assert max({_l::*}) is 9 with "max of a list was not 9"
	assert min(3, {_l::*}, 1) is -2 with "min of values and a list was not -2"
	assert sum({_l::*}, 10) is 21 with "sum was not 21"
	assert product(2, 3, 4) is 24 with "product(2, 3, 4) was not 24"
	assert mean(1, 2, 6) is 3 with "mean(1, 2, 6) was not 3"
	assert median(4, 1, 3, 2) is 2.5 with "median(4, 1, 3, 2) was not 2.5"
	assert max({_empty::*}) is not set with "max of an empty list had a value"

test "clamp":
	assert clamp(15, 0, 10) is 10 with "clamp(15, 0, 10) was not 10"
	assert clamp(-3, 10, 0) is 0 with "clamp with swapped bounds was not 0"
	set {_v::*} to 1, 50 and 7
	set {_c::*} to clamp({_v::*}, 2, 10)
	assert size of {_c::*} is 3 with "clamp of a list did not give 3 values"
	assert "%{_c::*}%" is "2, 10 and 7" with "clamp of a list gave %{_c::*}%"

test "counting":
	assert factorial(5) is 120 with "factorial(5) was not 120"
	assert factorial(-1) is not set with "factorial(-1) had a value"
	assert permutations(10, 2) is 90 with "permutations(10, 2) was not 90"
	assert combinations(10, 8) is 45 with "combinations(10, 8) was not 45"
	assert permutations(2, 3) is not set with "permutations(2, 3) had a value"
	assert calcExperience(30) is 1395 with "calcExperience(30) was not 1395"
