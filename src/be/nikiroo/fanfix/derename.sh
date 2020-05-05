#!/bin/sh

git status | grep renamed: | sed 's/[^:]*: *\([^>]*\) -> \(.*\)/\1>\2/g' | while read -r ln; do
	old="`echo "$ln" | cut -f1 -d'>'`"
	new="`echo "$ln" | cut -f2 -d'>'`"
	mkdir -p "`dirname "$old"`"
	git mv "$new" "$old"
	rmdir "`dirname "$new"`" 2>/dev/null
	true
done

