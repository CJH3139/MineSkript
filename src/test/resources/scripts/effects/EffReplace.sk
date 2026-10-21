# Tests for EffReplace: replacing text inside variables in place, ignoring capitals unless told otherwise.

local function exactly(a: text, b: text) :: boolean:
	if length of {_a} is not length of {_b}:
		return false
	if ({_a} with {_b} replaced with "" with case sensitivity) is "":
		return true
	return false

test "replace in a variable":
	set {_t} to "gg and GG"
	replace "gg" in {_t} with "good game"
	assert exactly({_t}, "good game and good game") with "replace in did not change every gg, gave %{_t}%"

test "replace with ... in":
	set {_t} to "a-b-c"
	replace all "-" with "+" in {_t}
	assert {_t} is "a+b+c" with "replace all with in did not work"
	set {_t} to "a-b-c"
	replace every "-" in {_t} with "."
	assert {_t} is "a.b.c" with "replace every in with did not work"

test "replace the first":
	set {_t} to "a-b-c"
	replace the first "-" with "+" in {_t}
	assert {_t} is "a+b-c" with "replace the first changed more than the first"
	set {_t} to "x-y-z"
	replace first "-" in {_t} with "+"
	assert {_t} is "x+y-z" with "replace first in with did not work"

test "several texts":
	set {_t} to "idiot and noob"
	replace all "idiot" and "noob" with "****" in {_t}
	assert {_t} is "**** and ****" with "several texts were not all replaced"

test "with case sensitivity":
	set {_t} to "Aa"
	replace "a" in {_t} with "b" with case sensitivity
	assert exactly({_t}, "Ab") with "with case sensitivity replaced a capital"
	set {_t} to "Aa"
	replace "A" with "c" in {_t} with case sensitivity
	assert exactly({_t}, "ca") with "with ... in ... with case sensitivity did not work"

test "list variables keep their indices":
	set {_l::x} to "one-1"
	set {_l::y} to "two-2"
	set {_l::z} to 3
	replace "-" in {_l::*} with " "
	assert {_l::x} is "one 1" with "the first entry was not changed"
	assert {_l::y} is "two 2" with "the second entry was not changed"
	assert {_l::z} is 3 with "an entry that is not text was changed"

test "several variables":
	set {_a} to "a!"
	set {_b} to "b!"
	replace "!" in {_a} and {_b} with "?"
	assert {_a} is "a?" with "the first variable was not changed"
	assert {_b} is "b?" with "the second variable was not changed"

test "unset variables stay unset":
	replace "a" in {_nothing} with "b"
	assert {_nothing} is not set with "replacing in an unset variable set it"
