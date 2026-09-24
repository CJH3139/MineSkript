# Text: the "" escape, %% and values inside text.

test "escaped quotes":
	set {_t} to "say ""hi"""
	assert {_t} contains """" with "a doubled quote did not become one quote"
	assert {_t} is "say ""hi""" with "text with escaped quotes did not compare equal to itself"

test "values in text":
	set {_n} to 5
	assert "n is %{_n}%" is "n is 5" with "a whole number in text did not print without decimals"
	assert "%1 + 1%" is "2" with "arithmetic inside text did not work"
	assert length of "100%%" is 4 with "a doubled percent sign counted as two characters"

test "case":
	assert ((uppercase "abc") with "ABC" replaced with "" with case sensitivity) is "" with "uppercase did not work"
	assert ((lowercase "ABC") with "abc" replaced with "" with case sensitivity) is "" with "lowercase did not work"
