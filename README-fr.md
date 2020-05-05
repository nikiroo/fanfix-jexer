[English](README.md) Français

# Fanfix-jexer

Fanfix-jexer est un programme qui offre une interface texte (via la librairie Jexer) à la librairie de téléchargement de comics/histoires/mangas [Fanfix](https://github.com/nikiroo/fanfix).

Vous pouvez aussi en avoir une version graphique, [Fanfix-swing](https://github.com/nikiroo/fanfix-swing).

## Synopsis

- ```fanfix-jexer```
- ```fanfix-jexer [...]``` (options [Fanfix](https://github.com/nikiroo/fanfix))

## Description

(Si vous voulez juste voir les derniers changements, vous pouvez regarder le [Changelog](changelog-fr.md) -- remarquez que le programme affiche le changelog si une version plus récente est détectée depuis la version x.x.x.)

![Main GUI](screenshots/fanfix-jexer.png?raw=true "Fenêtre principale")

Une gallerie de screenshots est disponible [ici](screenshots/README-fr.md).

Le fonctionnement du programme est assez simple : il converti une URL venant d'un site supporté en un fichier .epub pour les histoires ou .cbz pour les comics (d'autres options d'enregistrement sont disponibles, comme du texte simple, du HTML...).

Pour vous aider à organiser vos histoires, il peut aussi servir de bibliothèque locale vous permettant :

- d'importer une histoire depuis son URL (ou depuis un fichier)
- d'exporter une histoire dans un des formats supportés vers un fichier
- d'afficher une histoire **nativement** ou **en appelant un programme natif pour lire le fichier**

### Sites supportés

Pour le moment, les sites suivants sont supportés :

- http://FimFiction.net/ : fanfictions dévouées à la série My Little Pony
- http://Fanfiction.net/ : fanfictions venant d'une multitude d'univers différents, depuis les shows télévisés aux livres en passant par les jeux-vidéos
- http://mangahub.io/ : un site répertoriant une quantité non négligeable de mangas (English)
- https://e621.net/ : un site Furry proposant des comics, y compris de MLP
- https://sofurry.com/ : même chose, mais orienté sur les histoires plutôt que les images
- https://e-hentai.org/ : support ajouté sur demande : n'hésitez pas à demander un site !
- http://mangas-lecture-en-ligne.fr/ : un site proposant beaucoup de mangas, en français

### Types de fichiers supportés

Nous supportons les types de fichiers suivants (aussi bien en entrée qu'en sortie) :

- epub : les fichiers .epub créés avec Fanfix (nous ne supportons pas les autres fichiers .epub, du moins pour le moment)
- text : les histoires enregistrées en texte (.txt), avec quelques règles spécifiques :
	- le titre doit être sur la première ligne
	- l'auteur (précédé de rien, ```Par ```, ```De ``` ou ```©```) doit être sur la deuxième ligne, optionnellement suivi de la date de publication entre parenthèses (i.e., ```Par Quelqu'un (3 octobre 1998)```)
	- les chapitres doivent être déclarés avec ```Chapitre x``` ou ```Chapitre x: NOM DU CHAPTITRE```, où ```x``` est le numéro du chapitre
	- une description de l'histoire doit être donnée en tant que chaptire 0
	- une image de couverture peut être présente avec le même nom de fichier que l'histoire, mais une extension .png, .jpeg ou .jpg
- info_text : fort proche du format texte, mais avec un fichier .info accompagnant l'histoire pour y enregistrer quelques metadata (le fichier de metadata est supposé être créé par Fanfix, ou être compatible avec)
- cbz : les fichiers .cbz (une collection d'images zipées), de préférence créés avec Fanfix (même si les autres .cbz sont aussi supportés, mais sans la majorité des metadata de Fanfix dans ce cas)
- html : les fichiers HTML que vous pouvez ouvrir avec n'importe quel navigateur ; remarquez que Fanfix créera un répertoire pour y mettre les fichiers nécessaires, dont un fichier ```index.html``` pour afficher le tout -- nous ne supportons en entrée que les fichiers HTML créés par Fanfix

### Plateformes supportées

Toute plateforme supportant Java 1.6 devrait suffire.

Le programme a été testé sur Linux (Debian, Slackware et Ubuntu), MacOS X et Windows pour le moment, mais n'hésitez pas à nous informer si vous l'essayez sur un autre système.

Si vous avez des difficultés pour le compiler avec une version supportée de Java (1.6+), contactez-nous.

Note pour Windows : nous proposons aussi un laucnher exécutable au format EXE qui vérifie si Java est disponible avant de lancer le programme, et explique comment l'installer si pas.

## Options

Vous pouvez démarrer le programme sans paramètres :

- ```java -jar fanfix-jexer.jar```
- ```fanfix-jexer``` (si vous avez utilisé *make install*)

Vous pouvez aussi utiliser les options que [Fanfix](https://github.com/nikiroo/fanfix) supporte.

### Environnement

Certaines variables d'environnement sont reconnues par le programme :

- ```LANG=en```: forcer la langue du programme en anglais
- ```CONFIG_DIR=$HOME/.fanfix```: utilise ce répertoire pour les fichiers de configuration du programme (et copie les fichiers de configuration par défaut si besoin)
- ```NOUTF=1```: essaye d'utiliser des caractères non-unicode quand possible (cela peut avoir un impact sur les fichiers générés, pas uniquement sur les messages à l'utilisateur)
- ```DEBUG=1```: force l'option ```DEBUG=true``` du fichier de configuration (pour afficher plus d'information en cas d'erreur)

## Compilation

```./configure.sh && make```

Vous pouvez aussi importer les sources java dans, par exemple, [Eclipse](https://eclipse.org/), et faire un JAR exécutable depuis celui-ci.

### Librairies dépendantes (incluses)

Nécessaires :

- ```src/be/nikiroo/utils```: quelques utilitaires partagés, inclus en tant que subtree
- ```src/be/nikiroo/fanfix```: la librairie Fanfix sur laquelle tout le programme est basé, inclus en tant que subtree
- ```src/be/nikiroo/jexer```: la librairie jexer avec quelques modification, inclus en tant que subtree
- [```libs/unbescape-sources.jar```](https://github.com/unbescape/unbescape): une librairie sympathique pour convertir du texte depuis/vers beaucoup de formats ; utilisée ici pour la partie HTML
- [```libs/jsoup-sources.jar```](https://jsoup.org/): une libraririe pour parser du HTML
- [```libs/JSON-java-20190722-sources.jar```](https://github.com/stleary/JSON-java): une libraririe pour parser du JSON

Optionnelles :

- [```pandoc```](http://pandoc.org/): pour générer les man pages depuis les fichiers README, non inclu (pour l'utiliser, il faut que le programme ```pandoc``` soit disponible dans le PATH)

Rien d'autre, si ce n'est Java 1.6+.

À noter : ```make libs``` exporte ces librairies dans le répertoire src/.

## Auteur

Fanfix a été écrit par Niki Roo <niki@nikiroo.be>

