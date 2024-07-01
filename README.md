English [Français](README-fr.md)

# Fanfix-jexer

Fanfix-jexer is a program that offer you a text interface (via the Jexer librairy) around the comics/stories/mangas library [Fanfix](https://github.com/nikiroo/fanfix).

You can also use the graphical version, [Fanfix-swing](https://github.com/nikiroo/fanfix-swing).

## Synopsis

- ```fanfix-jexer```
- ```fanfix-jexer [...]``` ([Fanfix](https://github.com/nikiroo/fanfix) options)

## Description

(If you are interested in the recent changes, please check the [Changelog](changelog.md) -- note that starting from version x.x.x, the changelog is checked at startup.)

![Main GUI](screenshots/fanfix-jexer.png?raw=true "Main window")

A screenshots gallery can be found [here](screenshots/README.md).

It will convert from a (supported) URL to an .epub file for stories or a .cbz file for comics (a few other output types are also available, like Plain Text, LaTeX, HTML...).

To help organize your stories, it can also work as a local library so you can:

- Import a story from its URL (or just from a file)
- Export a story to a file (in any of the supported output types)
- Display a story from the local library **natively** or **by calling a native program to handle it**

### Supported websites

Currently, the following websites are supported:

- http://FimFiction.net/: fan fictions devoted to the My Little Pony show
- http://Fanfiction.net/: fan fictions of many, many different universes, from TV shows to novels to games
- http://mangahub.io/: a well filled repository of mangas (English)
- https://e621.net/: a Furry website supporting comics, including MLP
- https://sofurry.com/: same thing, but story-oriented
- https://e-hentai.org/: done upon request (so, feel free to ask for more websites!)
- http://mangas-lecture-en-ligne.fr/: a website offering a lot of mangas (in French)

### Support file types

We support a few file types for local story conversion (both as input and as output):

- epub: .epub files created by this program (we do not support "all" .epub files, at least for now)
- text: local stories encoded in plain text format, with a few specific rules:
	- the title must be on the first line
	- the author (preceded by nothing, ```by ``` or ```©```) must be on the second line, possibly with the publication date in parenthesis (i.e., ```By Unknown (3rd October 1998)```)
	- chapters must be declared with ```Chapter x``` or ```Chapter x: NAME OF THE CHAPTER```, where ```x``` is the chapter number
	- a description of the story must be given as chapter number 0
	- a cover image may be present with the same filename as the story, but a .png, .jpeg or .jpg extension
- info_text: contains the same information as the text format, but with a companion .info file to store some metadata (the .info file is supposed to be created by Fanfix or compatible with it)
- cbz: .cbz (collection of images) files, preferably created with Fanfix (but any .cbz file is supported, though without most of Fanfix metadata, obviously)
- html: HTML files that you can open with any browser; note that it will create a directory structure with ```index.html``` as the main file -- we only support importing HTML files created by Fanfix

### Supported platforms

Any platform with at lest Java 1.6 on it should be ok.

It has been tested on Linux (Debian, Slackware, Ubuntu), MacOS X and Windows for now, but feel free to inform us if you try it on another system.

If you have any problems to compile it with a supported Java version (1.6+), please contact us.

Note for Windows : we also offer a launcher in EXE format that checks if Java is available before starting the program, and helps you install it if not.

## Options

You can start the program without parameters:

- ```java -jar fanfix-jexer.jar```
- ```fanfix-jexer``` (if you used *make install*)

You can also use the options supported by [Fanfix](https://github.com/nikiroo/fanfix).

### Environment

Some environment variables are recognized by the program:

- ```LANG=en```: force the language to English
- ```CONFIG_DIR=$HOME/.fanfix```: use the given directory as a config directory (and copy the default configuration if needed)
- ```NOUTF=1```: try to fallback to non-unicode values when possible (can have an impact on the resulting files, not only on user messages)
- ```DEBUG=1```: force the ```DEBUG=true``` option of the configuration file (to show more information on errors)

## Compilation

```./configure.sh && make```

You can also import the java sources into, say, [Eclipse](https://eclipse.org/), and create a runnable JAR file from there.

### Dependant libraries (included)

Required:

- ```src/be/nikiroo/jexer```: the jexer library with some custom changes, included as a subtree
- [```libs/unbescape-sources.jar```](https://github.com/unbescape/unbescape): a nice library to escape/unescape a lot of text formats; used here for HTML
- [```libs/jsoup-sources.jar```](https://jsoup.org/): a library to parse HTML
- [```libs/JSON-java-20190722-sources.jar```](https://github.com/stleary/JSON-java): a library to parse JSON

Optional:

- [```pandoc```](http://pandoc.org/): to generate the man pages from the README files (to use it, ```pandoc``` must be available as program in the PATH)

Submodules:

- ```src/be/nikiroo/utils```: some shared utility functions from [https://github.com/nikiroo/nikiroo-utils.git](https://github.com/nikiroo/nikiroo-utils.git) -- branch ```subtree```
- ```src/be/nikiroo/fanfix```: the fanfix library upon which all the program is based from [https://github.com/nikiroo/fanfix.git](https://github.com/nikiroo/fanfix.git) -- branch ```subtree```

Nothing else but Java 1.6+.

Note that calling ```make libs``` will export the libraries into the src/ directory.

## Author

Fanfix was written by Niki Roo <niki@nikiroo.be>

