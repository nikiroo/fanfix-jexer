#
# Java makefile
# > NAME: main program name (for manual, jar, doc...)
# > MAIN: main source to compile without the 'src/' prefix nor the '.java' ext
# > MORE: more sources to compile to generate the full program
# > TEST: list of all test programs to compile and run (same format as MORE)
# > JAR_FLAGS : list of paths to include in the jar        file (`-C dir path`)
# > SJAR_FLAGS: list of paths to include in the source jar file (`-C dir path`)
# > PREFIX: the usual prefix to (un)install to -- you may of course override it
#
NAME  = fanfix-jexer
MAIN  = be/nikiroo/fanfix_jexer/Main
TEST  = 
JAR_MISC    = -C ./ LICENSE -C ./ VERSION -C libs/ licenses
JAR_FLAGS  += -C bin/ be -C bin/ org $(JAR_MISC)
SJAR_FLAGS += -C src/ be -C src/ org $(JAR_MISC)

PREFIX = /usr/local

#
# Special Options for this program: you can modify the previous var if needed
# > OPTION=non-default-value (or OPTION=default-value by default)
#
#ifeq ($(OPTION),non-default-value)
#MORE += be/nikiroo/utils/android/test/TestAndroid
#TEST += be/nikiroo/utils/android/ImageUtilsAndroid
#else    
#MORE += be/nikiroo/utils/ui/ImageUtilsAwt 
#MORE += be/nikiroo/utils/ui/ImageTextAwt
#TEST += be/nikiroo/utils/ui/test/TestUI 
#endif

################################################################################

JAVAC = javac
JAVAC_FLAGS += -encoding UTF-8 -d ./bin/ -cp ./src/
JAVA = java
JAVA_FLAGS += -cp ./bin/
JAR = jar
RJAR = java
RJAR_FLAGS += -jar

ifeq ($(DEBUG),1)
JAVAC_FLAGS += -Xlint:deprecation -g
endif

.PHONY: all build run clean mrpropre mrpropre love debug doc man test run-test \
	check_time jar sjar resources test-resources libs 

all: build jar sjar

check_time:
	@echo
	@echo ">>>>>>>>>> Checking lastest sources/build times..."
	@cp -a `find src  -type f -printf "%T@ %p\n" \
		| sort -n | cut -d' ' -f 2- | tail -n 1` latest_src
	@cp -a `find libs -type f -printf "%T@ %p\n" \
		| sort -n | cut -d' ' -f 2- | tail -n 1` latest_lib
	@cp -a `find . \( -wholename VERSION -o -name '*.md' -o \
		    \( -wholename './src/*' -a ! -name '*.java' \
			-a ! -wholename '*/test/*' \)\
		\) \
		-type f -printf "%T@ %p\n" 2>/dev/null \
		| sort -n | cut -d' ' -f 2- | tail -n 1` latest_rsc
	
	@ls latest_??? \
		--time-style=+@%Y-%m-%d\ %H:%M:%S -lt | cut -f2- -d@

build: check_time resources latest_bin
latest_bin: latest_src
	@echo
	@echo ">>>>>>>>>> Building sources..."
	$(JAVAC) $(JAVAC_FLAGS) "src/$(MAIN).java"
	@[ "$(MORE)" = "" ] || for sup in $(MORE); do \
		echo $(JAVAC) $(JAVAC_FLAGS) "src/$$sup.java"; \
		$(JAVAC) $(JAVAC_FLAGS) "src/$$sup.java"; \
	done;
	touch latest_bin


test: build test-resources latest_tst
latest_tst: latest_src
	@echo
	@echo ">>>>>>>>>> Building all tests: $(TEST)..."
	@if [ "$(TEST)" = "" ]; then \
		echo No test sources defined.; \
	else \
		for tst in $(TEST); do \
			echo $(JAVAC) $(JAVAC_FLAGS) "src/$$tst.java"; \
			$(JAVAC) $(JAVAC_FLAGS) "src/$$tst.java"; \
		done; \
	fi;
	touch latest_tst

# Main buildables
jar: libs resources $(NAME).jar
$(NAME).jar: latest_bin
	@echo
	@echo ">>>>>>>>>> Generating $@..."
	@if [ -d libs/bin/ ]; then \
		echo cp -r libs/bin/* bin/; \
		cp -r libs/bin/* bin/; \
	fi;
	cp -r src/* bin/
	@echo "Main-Class: `echo "$(MAIN)" | sed 's:/:.:g'`" > bin/manifest
	@echo >> bin/manifest
	$(JAR) cfm $(NAME).jar bin/manifest -C ./ *.md $(JAR_FLAGS)
	@if [ -e VERSION ]; then \
		echo cp $(NAME).jar "$(NAME)-`cat VERSION`.jar"; \
		cp $(NAME).jar "$(NAME)-`cat VERSION`.jar"; \
	fi;

sjar: libs $(NAME)-sources.jar
$(NAME)-sources.jar: latest_src
	@echo
	@echo ">>>>>>>>>> Generating $@..."
	@echo > bin/manifest
	$(JAR) cfm $(NAME)-sources.jar \
		bin/manifest -C ./ *.md $(SJAR_FLAGS);
	@if [ -e VERSION ]; then \
		echo cp $(NAME)-sources.jar \
			"$(NAME)-`cat VERSION`-sources.jar"; \
		cp $(NAME)-sources.jar \
			"$(NAME)-`cat VERSION`-sources.jar"; \
	fi;

# Requisites
libs: check_time latest_lib
latest_lib: bin/libs
bin/libs: 
	@echo
	@echo ">>>>>>>>>> Extracting sources from libs..."
	@cd src && \
	for lib in ../libs/*-sources.jar \
			../libs/*-sources.patch.jar; do \
		if [ "$$lib" != '../libs/*-sources.jar' \
			-a "$$lib" != '../libs/*-sources.patch.jar' ]; \
		then \
			echo cd src \&\& jar xf "$$lib"; \
			jar xf "$$lib"; \
		fi; \
	done;
	mkdir -p bin
	touch bin/libs

resources: check_time libs latest_rsc
latest_rsc: bin/VERSION
bin/VERSION:
	@echo
	@echo ">>>>>>>>>> Copying resources and documentation into bin/..."
	mkdir -p bin
	@if ! ls *.md 2>/dev/null 2>&1; then \
			echo touch bin/no-documentation.md; \
			touch bin/no-documentation.md; \
	else \
		echo cp *.md bin/; \
		cp *.md bin/; \
	fi;
	@cd src && find . | grep -v '\.java$$' \
		| grep -v '/test/' | while read -r ln; do \
		if [ -f "$$ln" ]; then \
			dir="`dirname "$$ln"`"; \
			mkdir -p "../bin/$$dir" ; \
			echo cp "$$ln" "../bin/$$ln" ; \
			cp "$$ln" "../bin/$$ln" ; \
		fi ; \
	done;
	cp VERSION bin/

test-resources: check_time resources latest_tsc
latest_tsc: latest_ttt
latest_ttt:
	@echo
	@echo ">>>>>>>>>> Copying test resources into bin/..."
	@cd src && find . | grep -v '\.java$$' \
		| grep '/test/' | while read -r ln; do \
		if [ -f "$$ln" ]; then \
			dir="`dirname "$$ln"`"; \
			mkdir -p "../bin/$$dir" ; \
			echo cp "$$ln" "../bin/$$ln" ; \
			cp "$$ln" "../bin/$$ln" ; \
		fi ; \
	done;
	touch latest_ttt

# Manual
man: 
	@echo
	@echo ">>>>>>>>>> Manual of $(NAME): $(MAKECMDGOALS)..."
	@$(MAKE) -f man.d $(MAKECMDGOALS) NAME=$(NAME)

# Run
run: build
	@echo
	@echo ">>>>>>>>>> Running $(NAME)..."
	$(JAVA) $(JAVA_FLAGS) $(MAIN)

# Run main test
run-test: 
	@echo
	@echo ">>>>>>>>>> Running tests: $(TEST)..."
	@[ "$(TEST)" != "" ] || echo No test sources defined.
	@if [ "`whereis tput`" = "tput:" ]; then \
	ok='"[ ok ]"'; \
	ko='"[ !! ]"'; \
	cols=80; \
	else \
	ok="`tput bold`[`tput setf 2` OK `tput init``tput bold`]`tput init`"; \
	ko="`tput bold`[`tput setf 4` !! `tput init``tput bold`]`tput init`"; \
	cols="`tput cols`"; \
	fi; \
	[ "$(TEST)"  = "" ] || ( \
		clear; \
		for test in $(TEST); do \
			echo $(JAVA) $(JAVA_FLAGS) \
				"$$test" "$$cols" "$$ok" "$$ko"; \
			$(JAVA) $(JAVA_FLAGS) "$$test" "$$cols" "$$ok" "$$ko"; \
		done; \
	);

# Doc/misc
doc: 
	@echo
	@echo ">>>>>>>>>> Generating documentation for $(NAME)..."
	doxygen
love:
	@echo " ...not war."
debug:
	$(MAKE) $(MAKECMDGOALS) PREFIX=$(PREFIX) NAME=$(NAME) DEBUG=1

# Clean
clean: 
	@echo
	@echo ">>>>>>>>>> Cleaning $(NAME)..."
	rm -rf bin/
	@for lib in libs/*-sources.jar libs/*-sources.patch.jar; do \
		if [ "$$lib" != 'libs/*-sources.jar' \
				-a "$$lib" != 'libs/*-sources.patch.jar' ]; \
		then \
			echo Cleaning `basename "$$lib"`...; \
			jar tf "$$lib" | while read -r ln; do \
				[ -f "src/$$ln" ] && rm "src/$$ln"; \
			done; \
			jar tf "$$lib" | tac | while read -r ln; do \
				if [ -d "src/$$ln" ]; then \
					rmdir "src/$$ln" 2>/dev/null || true; \
				fi; \
			done; \
		fi; \
	done;
	rm -f latest_???

mrproper: mrpropre
mrpropre: clean man
	@echo
	@echo ">>>>>>>>>> Calling Mr Propre..."
	rm -f $(NAME).jar
	rm -f $(NAME)-sources.jar
	rm -f "$(NAME)-`cat VERSION`.jar"
	rm -f "$(NAME)-`cat VERSION`-sources.jar"
	rm -rf doc/html doc/latex doc/man
	rmdir doc 2>/dev/null || true

# Install/uninstall
install: jar man
	@echo
	@echo ">>>>>>>>>> Installing $(NAME) into $(PREFIX)..."
	mkdir -p "$(PREFIX)/lib" "$(PREFIX)/bin"
	cp $(NAME).jar "$(PREFIX)/lib/"
	( \
	echo "#!/bin/sh"; \
	echo "$(RJAR) $(RJAR_FLAGS) \"$(PREFIX)/lib/$(NAME).jar\" \"\$$@\"" \
	) > "$(PREFIX)/bin/$(NAME)"
	chmod a+rx "$(PREFIX)/bin/$(NAME)"


uninstall: man
	@echo
	@echo ">>>>>>>>>> Uninstalling $(NAME) from $(PREFIX)..."
	rm -f "$(PREFIX)/bin/$(NAME)"
	rm -f "$(PREFIX)/lib/$(NAME).jar"
	rmdir "$(PREFIX)/bin" 2>/dev/null
	rmdir "$(PREFIX)/lib" 2>/dev/null

