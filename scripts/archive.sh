#!/usr/bin/env bash


archive() {
	local timestamp=$1

	if [ -e "archives/$timestamp.tar.gz" ] ; then
		return
	fi

	mkdir -p "archives/$timestamp"
	cp $(find logs -path "logs/$timestamp*" | xargs) "archives/$timestamp"
	tar czvf "archives/$timestamp.tar.gz" "archives/$timestamp"
	rm -rf "archives/$timestamp"
}


mkdir -p archives
for timestamp in $(find logs -path "logs/*" | sed 's/logs\///' | sed 's/_.*//') ; do
	archive "$timestamp"
done
