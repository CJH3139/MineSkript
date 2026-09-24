# if, else if, else, if all and if any, and try with on error.

test "if chains":
	set {_x} to 2
	if {_x} is 1:
		set {_branch} to "one"
	else if {_x} is 2:
		set {_branch} to "two"
	else:
		set {_branch} to "other"
	assert {_branch} is "two" with "the else if branch did not run"

test "if all":
	set {_x} to 5
	if all:
		{_x} is greater than 1
		{_x} is less than 10
	then:
		set {_all} to true
	assert {_all} is true with "if all did not run when every condition passed"
	if all:
		{_x} is greater than 1
		{_x} is greater than 10
	then:
		assert false with "if all ran when one condition failed"

test "if any":
	set {_x} to 5
	if any:
		{_x} is 1
		{_x} is 5
	then:
		set {_any} to true
	assert {_any} is true with "if any did not run when one condition passed"

test "try and on error":
	set {_n} to 1
	try:
		add "text" to {_n}
		set {_reached} to true
	on error:
		set {_caught} to {_error}
	assert {_reached} is not set with "the line after an error in a try still ran"
	assert {_caught} is set with "on error did not run"
	assert {_caught} contains "cannot add" with "the error message was %{_caught}%"

test "try without an error":
	try:
		set {_ok} to true
	on error:
		assert false with "on error ran without an error"
	assert {_ok} is true with "the try body did not run"
