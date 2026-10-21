# Tests for ExprStringCase: upper case, lower case, capitalized and proper case. Plain is ignores capitals, so the
# results are checked with a case sensitive replace.

local function exactly(a: text, b: text) :: boolean:
	if length of {_a} is not length of {_b}:
		return false
	if ({_a} with {_b} replaced with "" with case sensitivity) is "":
		return true
	return false

test "upper and lower case":
	assert exactly(uppercase "abc", "ABC") with "uppercase did not work"
	assert exactly(upper case "abc", "ABC") with "upper case did not work"
	assert exactly(lowercase "ABC", "abc") with "lowercase did not work"
	assert exactly(lower case "ABC", "abc") with "lower case did not work"
	assert exactly("Oops!" in lower case, "oops!") with "in lower case did not work"
	assert exactly("oops!" in uppercase, "OOPS!") with "in uppercase did not work"

test "capitalized means all capitals, as in Skript":
	assert exactly(capitalized "hello there", "HELLO THERE") with "capitalized did not give all capitals"
	assert exactly(capitalised "x", "X") with "capitalised did not work"

test "proper case":
	assert exactly("hellO i'm steve!" in proper case, "HellO I'm Steve!") with "in proper case did not work"
	assert exactly(title case "the end", "The End") with "title case did not work"
	assert exactly("hellO i'm steve!" in strict proper case, "Hello I'm Steve!") with "strict proper case did not work"
	assert exactly(lenient proper case "aB cD", "AB CD") with "lenient proper case did not work"

test "a list of texts":
	set {_l::*} to "a" and "b"
	set {_u::*} to uppercase {_l::*}
	assert size of {_u::*} is 2 with "uppercase of a list did not give two texts"
	assert exactly({_u::2}, "B") with "the second text was not B"
