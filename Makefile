JAVA=java
JAVAC=javac
JFLEX=jflex
CUPJAR=./lib/java-cup-11b.jar
CUPRUNTIME=./lib/java-cup-11b-runtime.jar
CUP=$(JAVA) -cp $(CUPJAR) java_cup.Main

all:
	mkdir -p bin
	$(JFLEX) -d . src/scanner/cminus.flex
	$(CUP) -destdir . -expect 5 src/parser/cminus.cup
	$(JAVAC) -cp $(CUPRUNTIME):. -d bin src/absyn/*.java Lexer.java sym.java src/CM.java

clean:
	rm -rf bin
	rm -f Lexer.java sym.java parser.java

.PHONY: all clean