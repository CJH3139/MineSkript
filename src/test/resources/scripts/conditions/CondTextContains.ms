# Tests for CondTextContains, CondStartsWith, CondEndsWith and CondIgnoringCase. Text checks ignore capitals, like Skript.

test "contains":
	assert "hello world" contains "lo w" with "contains did not find text in the middle"
	assert "hello" contains "Hello" with "contains did not ignore capitals"
	assert "Hello World" contains "WORLD" with "contains did not ignore capitals in the longer text"
	assert "hello" doesn't contain "xyz" with "contains found text that is not there"

test "starts and ends with":
	assert "!help" starts with "!" with "starts with did not match the first character"
	assert "why?" ends with "?" with "ends with did not match the last character"
	assert "hello" doesn't start with "e" with "starts with matched a later character"
	assert "Hello" starts with "hE" with "starts with did not ignore capitals"
	assert "Hello" ends with "LO" with "ends with did not ignore capitals"
	assert "lo" doesn't end with "hello" with "a short text ended with a longer one"

test "ignoring case":
	assert "GG" is "gg" ignoring case with "ignoring case still compared capitals"
	assert "GG" is not "gh" ignoring case with "ignoring case matched different text"
