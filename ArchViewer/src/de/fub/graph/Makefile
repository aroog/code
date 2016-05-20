# $Id: Makefile,v 1.1 1999/08/17 14:36:41 dahm Exp $

SOURCES := $(wildcard *.java)
CLASSES := $(patsubst %.java,%.class,$(SOURCES)) 
JC      := jikes
JFLAGS  := +E #+\$$

#
# New rule for type dependent compilation
#
%.class: %.java
	@rm -f $@
	$(JC) $(JFLAGS) $<

all: $(CLASSES)

clean:
	rm -f core *~ *.class jens*
