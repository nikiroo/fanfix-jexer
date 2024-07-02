# Requires variable: NAME

.PHONY: doc clean mrpropre mrproper

doc: VERSION Doxyfile
	@if doxygen -v >/dev/null 2>&1; then \
		echo Adding VERSION number to Doxyfile...; \
		tmp=`mktemp`; \
		grep -v '^PROJECT_NUMBER' Doxyfile > "$$tmp"; \
		cat "$$tmp" > Doxyfile; \
		rm -f "$$tmp"; \
		echo "PROJECT_NUMBER	 = `cat VERSION`" >> Doxyfile; \
		echo doxygen; \
		doxygen; \
	else \
		echo "man pages generation: pandoc required" >&2; \
		false; \
	fi; \

clean:
	@( \
		echo Removing VERSION number from Doxyfile...; \
		tmp=`mktemp`; \
		grep -v '^PROJECT_NUMBER' Doxyfile > "$$tmp"; \
		cat "$$tmp" > Doxyfile; \
		rm -f "$$tmp"; \
	);

mrproper: mrpropre
mrpropre:
	rm -rf doc/html doc/latex doc/man
	rmdir doc 2>/dev/null || true

