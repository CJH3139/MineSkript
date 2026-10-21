# Tests for ExprListSize.

test "size of lists":
	set {_l::*} to "a", "b" and "c"
	assert size of {_l::*} is 3 with "size of a three entry list was not 3"
	assert amount of {_l::*} is 3 with "amount of did not work"
	assert number of {_l::*} is 3 with "number of did not work"
	assert size of {_missing::*} is 0 with "an unset list did not have size 0"
	assert size of ("a b" split at " ") is 2 with "size of a split text was not 2"
