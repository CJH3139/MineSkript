# Tests for the built-in location function registered by WorldFunctions.

test "location":
	set {_l} to location(1, 2.5, -3)
	assert x-coordinate of {_l} is 1 with "the x-coordinate was not 1"
	assert {_l}'s y-coordinate is 2.5 with "the y-coordinate was not 2.5"
	assert z-coordinate of {_l} is -3 with "the z-coordinate was not -3"
	assert "%{_l}%" is "x: 1, y: 2.5, z: -3 in minecraft:overworld" with "a location printed as %{_l}%"

test "location in another dimension":
	set {_l} to location(0, 64, 0, "the_nether")
	assert "%{_l}%" is "x: 0, y: 64, z: 0 in minecraft:the_nether" with "the dimension was not added"
	assert {_l} is not location(0, 64, 0) with "locations in different dimensions were equal"
	assert location(0, 64, 0) is location(0, 64, 0, "minecraft:overworld") with "the default dimension was wrong"

test "a location can be saved in a list":
	add location(1, 1, 1) to {_homes::*}
	add location(2, 2, 2) to {_homes::*}
	assert size of {_homes::*} is 2 with "two locations were not added"
