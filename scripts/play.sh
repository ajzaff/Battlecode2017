#!/usr/bin/env bash

# Teams matrix
teams=""
teams+="team419 "
teams+="examplefuncsplayer "

# Maps matrix
maps=""
maps+="Barrier "
maps+="DenseForest "
maps+="Enclosure "
maps+="Hurdle "
maps+="SparseForest "
maps+="shrine "
map_list=$(echo $maps | sed 's/ /,/g')

mkdir -p logs/
datetime=$(date -Iseconds)

play() {
	local team_a="$1"
	local team_b="$2"

	if [ -n "$3" ] ; then
		local map_list="$3"
	elif [ "$team_a" = "$team_b" ] ; then
		return
	fi

	echo
	echo "#########################################"
	echo "${team_a} vs. ${team_b} on ${map_list}"
	echo "#########################################"
	echo

	bash gradlew run -q -PteamA=$team_a -PteamB=$team_b -Pmaps=$map_list 2>&1 | tee "logs/${datetime}_${team_a}_${team_b}"
}

if [ "$#" = 0 ] ; then
for t_a in $teams ; do
for t_b in $teams ; do
	play "$t_a" "$t_b"
	play "$t_b" "$t_a"
done
done
elif [ "$#" = 1 ] ; then
for t_b in $teams ; do
	play "$1" "$t_b"
	play "$t_b" "$1"
done
elif [ "$#" = 2 ] ; then
	play "$1" "$2" "$map_list"
	play "$2" "$1" "$map_list"
elif [ "$#" = 3 ] ; then
	play "$1" "$2" "$3" "$map_list"
	play "$2" "$1" "$3" "$map_list"
fi
