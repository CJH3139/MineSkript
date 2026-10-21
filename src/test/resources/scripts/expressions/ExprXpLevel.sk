# Tests for the Skript property forms of the player's values: ExprXpLevel (level of player) and its
# relatives ExprXpProgress, ExprTotalExperience, ExprAir, ExprRemainingAir, ExprMaxAir, ExprSaturation,
# ExprHunger, ExprMaxHealth, ExprYaw, ExprPitch, ExprSelectedSlot, ExprHeldItem, ExprOffhandItem,
# ExprTargetBlock, ExprTargetEntity and ExprVehicle. The fake player has default values.

test "experience":
	assert player's level is 0 with "player's level was not 0"
	assert level of player is xp level with "level of player was not the xp level"
	assert player's level progress is 0 with "level progress was not 0"
	assert experience of player is 0 with "experience of player was not 0"
	assert player's total experience is total xp with "the total experience forms differed"

test "air":
	assert player's remaining air is 15 seconds with "remaining air was not 15 seconds"
	assert remaining air of player is greater than 3 seconds with "remaining air was short"
	assert player's air is 300 with "player's air was not 300"
	assert max air of player is 300 with "max air of player was not 300"
	assert player's maximum air is 300 with "maximum air was not 300"

test "food and health":
	assert player's saturation is 5 with "saturation was not 5"
	assert saturation of player is saturation with "the saturation forms differed"
	assert food level is 20 with "food level was not 20"
	assert player's hunger bar is 20 with "hunger bar was not 20"
	assert food meter of me is 20 with "food meter was not 20"
	assert hunger of player is 20 with "the old hunger form was not 20"
	assert max health is 20 with "max health was not 20"
	assert player's maximum health is 20 with "maximum health was not 20"

test "rotation":
	assert player's yaw is 0 with "yaw was not 0"
	assert pitch of player is 0 with "pitch was not 0"
	set player's yaw to 90
	assert yaw is 90 with "setting player's yaw did not turn"
	add 10 to pitch of player
	assert player's pitch is 10 with "adding to pitch of player did not tilt"

test "the hotbar slot":
	assert player's hotbar slot is 0 with "hotbar slot was not 0"
	set player's hotbar slot to 4
	assert selected slot is 4 with "setting the hotbar slot did not select it"
	assert current hotbar slot is 4 with "current hotbar slot was not 4"
	assert the currently selected hotbar slot of player is 4 with "the long form was not 4"

test "held items, targets and the vehicle":
	assert player's tool is air with "an empty hand was not air"
	assert weapon of player is held item with "weapon of player was not the held item"
	assert player's offhand item is air with "an empty offhand was not air"
	assert off hand tool of me is air with "off hand tool was not air"
	assert player's targeted block is air with "the targeted block was not air"
	assert target block of player is air with "target block of player was not air"
	assert target of player is not set with "there was a target entity"
	assert player's vehicle is not set with "there was a vehicle"
