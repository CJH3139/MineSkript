# Tests for ExprGamemode: the game mode type, its literals and its Skript property forms. Also covers
# CondGamemode, the older text form. The fake game starts in survival.

test "the gamemode is a game mode value":
	assert gamemode is survival with "gamemode was not survival"
	assert player's gamemode is survival with "player's gamemode was not survival"
	assert game mode of player is survival with "game mode of player was not survival"
	assert gamemode of me is not creative with "survival was creative"
	assert gamemode is creative or survival with "an or list of game modes did not match"
	assert gamemode is not creative or spectator with "a negated or list matched"

test "text still compares with the gamemode":
	assert gamemode is "survival" with "gamemode was not the text survival"
	assert gamemode is "SURVIVAL" with "the text comparison cared about capitals"
	assert player's gamemode is not "creative" with "gamemode was the text creative"
	assert "%gamemode%" is "survival" with "gamemode did not print as survival"

test "a gamemode in a variable":
	set {_g} to player's gamemode
	assert {_g} is survival with "a saved gamemode was not survival"
	assert {_g} is "survival" with "a saved gamemode was not the text survival"
