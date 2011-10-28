###############################################################
# Makefile for mspsim
#
# Needed stuff in the PATH:
#  java, javac (JDK 1.5 or newer)
#
# Under MS-DOS/Windows
#  A GNU compatible Make (for example Cygwin's)
###############################################################

###############################################################
# Settings
###############################################################
CC=javac -g
JAVA=java
JAR=jar
RM=rm -f

###############################################################
# System dependent
###############################################################

ifndef WINDIR
  ifdef OS
    ifneq (,$(findstring Windows,$(OS)))
      WINDIR := Windows
    endif
  endif
endif

ifndef WINDIR
  # This settings are for UNIX
  SEPARATOR=:
  # Add "'" around filenames when removing them because UNIX expands "$"
  APO='#'  (last apostrophe to avoid incorrect font-lock)
else
  # These setting are for Windows
  SEPARATOR=;
  APO=
endif


###############################################################
# Arguments
###############################################################

EMPTY :=
SPACE := ${EMPTY} ${EMPTY}
LIBS := ${wildcard lib/*.jar}
CLASSPATH=${subst ${SPACE},${SEPARATOR},. ${LIBS}}
CCARGS=-deprecation -classpath "${CLASSPATH}"

JAVAARGS=-classpath "${CLASSPATH}"


###############################################################
# SERVER OBJECTS
###############################################################

ifndef FIRMWAREFILE
ESBFIRMWARE = firmware/esb/sensor-demo.firmware
SKYFIRMWARE = firmware/sky/blink.firmware
Z1FIRMWARE = firmware/z1/blink.firmware
WISPFIRMWARE = firmware/wisp/dlwisp41.firmware
else
ESBFIRMWARE = ${FIRMWAREFILE}
SKYFIRMWARE = ${FIRMWAREFILE}
Z1FIRMWARE = ${FIRMWAREFILE}
endif

CPUTEST := tests/cputest.firmware
TIMERTEST := tests/timertest.firmware

SCRIPTS := ${addprefix scripts/,autorun.sc duty.sc}
BINARY := README.txt license.txt CHANGE_LOG.txt images/*.jpg firmware/*/*.firmware ${SCRIPTS}

PACKAGES := se/sics/mspsim edu/umass/energy ${addprefix se/sics/mspsim/,core chip cli config debug platform ${addprefix platform/,esb sky jcreate sentillausb z1 wisp} plugin profiler net ui util extutil/highlight extutil/jfreechart}

SOURCES := ${wildcard *.java $(addsuffix /*.java,$(PACKAGES))}

OBJECTS := $(SOURCES:.java=.class)

JARFILE := mspsim.jar

###############################################################
# MAKE
###############################################################

.PHONY: all compile jar help run runesb runsky test cputest $(CPUTEST) mtest runwisp

all:	compile

compile:	$(OBJECTS)

jar:	compile JarManifest.txt
	$(JAR) cfm $(JARFILE) JarManifest.txt ${addsuffix /*.class,$(PACKAGES)} images/*.jpg
	-$(RM) JarManifest.txt

JarManifest.txt:
	@echo >>$@ "Manifest-Version: 1.0"
	@echo >>$@ "Sealed: true"
	@echo >>$@ "Main-Class: se.sics.mspsim.Main"
	@echo >>$@ "Class-path: ${LIBS}"

help:
	@echo "Usage: make [all,compile,clean,run,runsky,runesb,runwisp]"

run:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.util.IHexReader $(ARGS) $(FIRMWAREFILE) $(MAPFILE)

runesb:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.platform.esb.ESBNode $(ARGS) $(ESBFIRMWARE) $(MAPFILE)

runsky:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.platform.sky.SkyNode $(ARGS) $(SKYFIRMWARE) $(MAPFILE)

runskyprof:	compile
	$(JAVA) -agentlib:yjpagent $(JAVAARGS) se.sics.mspsim.platform.sky.SkyNode $(ARGS) $(SKYFIRMWARE) $(MAPFILE)

runtelos:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.platform.sky.TelosNode $(ARGS) $(SKYFIRMWARE) $(MAPFILE)
runz1:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.platform.z1.Z1Node $(ARGS) $(Z1FIRMWARE) $(MAPFILE)

runwisp:	compile
	$(JAVA) $(JAVAARGS) se.sics.mspsim.platform.wisp.WispNode $(ARGS) $(WISPFIRMWARE) -nogui -autorun=scripts/wisp.sc $(MAPFILE)


test:	cputest footest

cputest:	$(CPUTEST)
	$(JAVA) $(JAVAARGS) se.sics.mspsim.util.Test $(CPUTEST)

timertest:	$(TIMERTEST)
	$(JAVA) $(JAVAARGS) se.sics.mspsim.util.Test $(TIMERTEST)

$(CPUTEST):
	(cd tests && $(MAKE))
$(TIMERTEST):
	(cd tests && $(MAKE))

mtest:	compile $(CPUTEST)
	@-$(RM) mini-test_cpu.txt
	$(JAVA) $(JAVAARGS) se.sics.util.Test -debug $(CPUTEST) >mini-test_cpu.txt


###############################################################
# ARCHIVE GENERATION
###############################################################

source:
	zip -9 mspsim-source-`date '+%F'`.zip Makefile $(BINARY) $(addsuffix /*.java,$(PACKAGES)) tests/Makefile tests/*.c tests/*.h lib/*.*


###############################################################
# CLASS COMPILATION
###############################################################

%.class : %.java
	$(CC) $(CCARGS) $<


###############################################################
# CLEAN  (untrusted, use with great care!!!)
###############################################################

.PHONY:	clean

clean:
ifdef WINDIR
	-$(RM) *.class ${addsuffix /*.class,$(PACKAGES)}
else
	-$(RM) $(foreach f,$(wildcard *.class),$(APO)$(f)$(APO)) $(foreach dir,$(PACKAGES),$(dir)/*.class)
endif
