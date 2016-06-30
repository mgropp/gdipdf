gdipdf
======

Java → PDF für eine Programmier-Einführungs-Veranstaltung


Voraussetzungen
---------------
Benötigt werden:
 * ant http://ant.apache.org/
 * ivy http://ant.apache.org/ivy/

Optional:
 * JFlex http://jflex.de/ (falls man den Lexer selbst generieren will)


Will man ohne ant+ivy arbeiten, muss man selbst die folgenden
Bibliotheken herunterladen:
 * iText http://itextpdf.com/
 * icu4j http://site.icu-project.org/
 * args4j http://args4j.kohsuke.org/

Klonen & Bauen
---------------
```
git clone git://redmine.cs.fau.de/gdipdf.git
cd gdipdf
ant
```

Zum Aktualisieren der Build-Information im Quelltext:
```
ant stamp
```

Eclipse
-------
Im Repository sind keine Eclipse-Projekt-Daten enthalten, ist also ein
bisschen umständlich.

 * Neues Eclipse-Projekt gdipdf anlegen
 * Repository in temporäres Verzeichnis klonen:
 
```
cd /tmp
git clone git://redmine.cs.fau.de/gdipdf.git
```

 * Dateien aus dem temp. Verzeichnis ins Eclipse-Verzeichnis kopieren:

```
cd ~/workspace/gdipdf
cp -r /tmp/gdipdf/* .
cp -r /tmp/gdipdf/.git .
```

 * Benötigte Bibliotheken herunterladen:

```
ant resolve
```

(Falls man kein ant+ivy hat, muss man die o.g. Bibliotheken stattdessen
selbst herunterladen.)

* In Eclipse:
  Projekt-Ordner aktualisieren,
  alle Jars aus `lib/` in den Build Path aufnehmen,
  `src-generated/` als Source Folder hinzufügen.
