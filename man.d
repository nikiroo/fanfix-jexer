# Requires variables: NAME, PREFIX (for install and uninstall only)

.PHONY: man clean mrpropre mrproper install uninstall

man: VERSION README.md README*.md
	@if pandoc -v >/dev/null 2>&1; then \
		ls README*.md 2>/dev/null \
				| grep 'README\(-..\|\)\.md' \
				| while read man; do \
			lang="`echo "$$man" \
				| sed 's:README\.md:en:' \
				| sed 's:README-\(.*\)\.md:\1:'`"; \
			echo "Processing language: $$lang..."; \
			echo mkdir -p man/"$$lang"/man1; \
			mkdir -p man/"$$lang"/man1; \
			echo "pandoc [...] > man/$$lang"/man1/"${NAME}.1"; \
			( \
				echo ".TH \"${NAME}\" 1 `\
					date +%Y-%m-%d\
					` \"version `cat VERSION`\""; \
				echo; \
				UNAME="`echo "${NAME}" \
					| sed 's:\(.*\):\U\1:g'`"; \
				( \
					cat "$$man" | head -n1 \
	| sed 's:.*(README\(-..\|\)\.md).*::g'; \
					cat "$$man" | tail -n+2; \
				) | sed 's:^#\(#.*\):\1:g' \
	| sed 's:^\(#.*\):\U\1:g;s:# *'"$$UNAME"':# NAME\n'"${NAME}"' \\- :g' \
	| sed 's:--:——:g' \
	| pandoc -f markdown -t man | sed 's:——:--:g' ; \
			) > man/"$$lang"/man1/"${NAME}.1"; \
		done; \
		echo mkdir -p "man/man1"; \
		mkdir -p "man/man1"; \
		echo cp man/en/man1/"${NAME}".1 man/man1/; \
		cp man/en/man1/"${NAME}".1 man/man1/; \
	else \
		echo "man pages generation: pandoc required" >&2; \
		false; \
	fi; \

clean:
	# nothing to clean for the manual
mrproper: mrpropre
mrpropre: clean
	rm -f man/man1/*.1 man/*/man1/*.1
	rmdir man/*/man1 man/* man 2>/dev/null || true 

install:
	@if [ -e "man/man1/$(NAME).1" ]; then \
		echo mkdir -p "$(PREFIX)"/share/man; \
		mkdir -p "$(PREFIX)"/share/man; \
		echo cp -r man "$(PREFIX)"/share/; \
		cp -r man "$(PREFIX)"/share/; \
	else \
		echo "No manual has been built (see \`make man')"; \
	fi

uninstall:
	@if [ -e "man/man1/$(NAME).1" ]; then \
		find man/ -type f | while read -r page; do \
			echo rm "$(PREFIX)/share/$$page";\
			rm "$(PREFIX)/share/$$page";\
		done; \
		echo rmdir "$(PREFIX)/share/man" 2>/dev/null \|\| true; \
		rmdir "$(PREFIX)/share/man" 2>/dev/null || true; \
		rmdir "$(PREFIX)/share"     2>/dev/null || true; \
	else \
		echo "No manual has been built (see \`make man')"; \
	fi

