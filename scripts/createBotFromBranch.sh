#!/usr/bin/env bash

start_branch=$(git branch | sed -n 's/^\* //p')

if [ "$#" = 0 ] ; then
	branch=$start_branch
else
	branch=$1
	git checkout $1 > /dev/null 2>&1
fi


if [ "$?" != 0 ] ; then
	echo "fatal! branch of ref not found: $branch"
else

	if [ -e "src/$branch" ] ; then
		echo "fatal! refusing to overwrite directory $branch"
	else

		mkdir -p "src/$branch"

		for file in $(find "src/team419" -name '*.java') ; do
			new_file=$(echo $file | sed "s/src\/team419\//src\/$branch\//")
			echo processing $file into $new_file ...
			cat $file | sed "s/package team419;/package $branch;/" | sed "s/import static team419\./import static $branch\./g" > "$new_file"
		done
	fi
fi

if [ ! "$#" = 0 ] ; then
	git checkout $start_branch > /dev/null 2>&1
fi
