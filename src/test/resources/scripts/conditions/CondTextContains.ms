# Tests for CondTextContains, CondStartsWith, CondEndsWith and CondIgnoringCase.

test "contains":
	assert "hello world" contains "lo w" with "contains did not find text in the middle"
	assert "hello" does not contain "Hello" with "contains ignored capitals"
	assert "hello" doesn't contain "xyz" with "contains found text that is not there"

test "starts and ends with":
	assert "!help" starts with "!" with "starts with did not match the first character"
	assert "why?" ends with "?" with "ends with did not match the last character"
	assert "hello" doesn't start with "e" with "starts with matched a later character"

test "ignoring case":
	assert "GG" is "gg" ignoring case with "ignoring case still compared capitals"
	assert "GG" is not "gh" ignoring case with "ignoring case matched different text"
