JAVA=java
JAVAC=javac
JFLEX=/opt/homebrew/bin/jflex
CUPJAR=./lib/java-cup-11b.jar
CUPRUNTIME=./lib/java-cup-11b-runtime.jar
CUP=$(JAVA) -cp $(CUPJAR) java_cup.Main

all:
	mkdir -p bin
	$(JFLEX) -d src/scanner src/scanner/cminus.flex
	$(CUP) -destdir src/scanner -parser parser src/parser/cminus.cup
	$(JAVAC) -cp $(CUPRUNTIME):. -d bin src/scanner/Lexer.java src/scanner/sym.java

clean:
	rm -rf bin
	rm -f src/scanner/Lexer.java
	rm -f src/scanner/sym.java
	rm -f src/scanner/parser.java

.PHONY: all clean