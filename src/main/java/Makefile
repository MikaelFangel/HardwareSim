antlr4 = java org.antlr.v4.Tool
grun = java org.antlr.v4.gui.TestRig
SRCFILES = main.java AST.java Environment.java
GENERATED = hwsimParser.java hwsimBaseVisitor.java hwsimVisitor.java hwsimLexer.java

all:
	make tree

main.class:	$(SRCFILES) $(GENERATED) hwsim.g4
	javac  $(SRCFILES) $(GENERATED)

hwsimLexer.java:	hwsim.g4
	$(antlr4) -visitor hwsim.g4

hwsimLexer.class: hwsimLexer.java
	javac $(GENERATED)

test:	hwsimLexer.java main.class input.txt
	java main input.txt

tree:	hwsimLexer.class hwsim.g4 input.txt
	$(grun) hwsim start -gui -tokens input.txt

clean:
	rm -rf hwsim*.java hwsim*.interp hwsim*.tokens *.class
