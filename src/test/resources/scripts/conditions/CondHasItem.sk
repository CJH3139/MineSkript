# Tests for CondHasItem: has and contains with item types, amounts and inventories. Also covers
# ExprInventory, ExprInventoryCount and the Skript slot form of ExprItemInSlot. The fake inventory is empty.

test "an empty inventory has nothing":
	assert player doesn't have diamond with "an empty inventory had a diamond"
	assert player does not have 3 diamonds with "an empty inventory had 3 diamonds"
	assert player do not have 64 of stone with "an empty inventory had 64 stone"
	assert player's inventory doesn't have stone with "player's inventory had stone"
	assert player doesn't have stone in their inventory with "the inventory form had stone"
	assert inventory of player doesn't contain dirt with "inventory of player contained dirt"
	assert player's inventory does not contain 2 torches with "player's inventory contained torches"
	assert player doesn't have diamond or stone with "an or list matched an empty inventory"

test "counting items in an inventory":
	assert amount of diamond in player's inventory is 0 with "amount of diamond was not 0"
	assert number of diamonds of inventory of player is 0 with "number of diamonds was not 0"
	assert amount of diamond and stone in player's inventory is 0 with "a list of items was not 0"
	assert number of diamond in inventory is 0 with "the old form was not 0"

test "slots of an inventory":
	assert slot 0 of player's inventory is air with "an empty slot was not air"
	assert player's inventory's slot 40 is air with "the offhand slot was not air"
	assert slot 3 of player is air with "slot of player was not air"

test "the inventory prints with the player's name":
	assert "%player's inventory%" is "inventory of Steve" with "the inventory printed as %player's inventory%"
	assert player's inventory is empty with "an empty inventory was not empty"
	assert player's inventory is not full with "an empty inventory was full"

test "list contains is not an inventory check":
	set {_list::*} to stone and dirt
	assert {_list::*} contains stone with "a list lost its contains check"
	assert {_list::*} does not contain diamond with "a list contained diamond"
