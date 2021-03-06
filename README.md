# Scalabha

Author: **Jason Baldridge** (jasonbaldridge@gmail.com)


## Introduction

This is to be a package for helping teach Computational Linguistics
using Scala. No aspirations in particular to be like NLTK, just
something to provide some basic functionality and a build structure
for students.

It's called Scalabha because "bha" is a Proto-Indo-European root that
is connected with language and speech.

## Requirements

* Version 1.6 of the Java 2 SDK (http://java.sun.com)

## Configuring your environment variables

The easiest thing to do is to set the environment variables `JAVA_HOME`
and `SCALABHA_DIR` to the relevant locations on your system. Set `JAVA_HOME`
to match the top level directory containing the Java installation you
want to use.

For example, on Windows:

	C:\> set JAVA_HOME=C:\Program Files\jdk1.5.0_04

or on Unix:

	(csh)
	% setenv JAVA_HOME /usr/local/java
  
	(ksh, bash)	
	> export JAVA_HOME=/usr/java

On Windows, to get these settings to persist, it's actually easiest to
set your environment variables through the System Properties from the
Control Panel. For example, under WinXP, go to Control Panel, click on
System Properties, choose the Advanced tab, click on Environment
Variables, and add your settings in the User variables area.

Next, likewise set `SCALABHA_DIR` to be the top level directory where you
unzipped the Scalabha download. In Unix, type `pwd` in the directory
where this file is and use the path given to you by the shell as
`SCALABHA_DIR`.  You can set this in the same manner as for `JAVA_HOME`
above.

Next, add the directory `SCALABHA_DIR/bin` to your path. For example, you
can set the path in your `.bashrc` file as follows:

	export PATH=$PATH:$SCALABHA_DIR/bin

Once you have taken care of these three things, you should be able to
build and use the Scalabha Library.

Note: Spaces are allowed in `JAVA_HOME` but not in `SCALABHA_DIR`.  To set
an environment variable with spaces in it, you need to put quotes around the value when on Unix, but you must *NOT* do this when under Windows.


## Building the system from source

Scalabha uses SBT (Simple Build Tool) with a standard directory
structure.  To build Scalabha, type (in the `$SCALABHA_DIR` directory):

	$ scalabha build update compile

This will compile the source files and put them in
`./target/classes`. If this is your first time running it, you will see
messages about Scala being downloaded -- this is fine and
expected. Once that is over, the Scalabha code will be compiled.

To try out other build targets, do:

	$ scalabha build

This will drop you into the SBT interface. To see the actions that are
possible, hit the TAB key. (In general, you can do auto-completion on
any command prefix in SBT, hurrah!)

Documentation for SBT is at <https://github.com/harrah/xsbt/wiki>

Note: if you have SBT 0.11.1 already installed on your system, you can
also just call it directly with "sbt" in `SCALABHA_DIR`.


## Trying it out

Assuming you have completed all of the above steps, including running
the "compile" action in SBT, you should now be able to try out some
examples, to be added.


## Now what?

One purpose of this package is to allow people to easily build a jar
of their own without needing anything other than the command line and
Java. You should be able to adapt the SBT build to your own project
and start creating your own packages based on these fairly
straightforwardly. You'll want to:

 * Change `$SCALABHA_DIR/build.sbt` properties and configurations to be
   appropriate for your project. If you need to specify new managed
   dependencies, you can do so easily in that file (see SBT
   documentation for details). If you prefer to add dependencies
   manually, just add them to `$SCALABHA_DIR/lib` and they'll get picked
   up without any fuss.

 * Change `$SCALABHA_DIR/bin` to be an executable of your choice, named
   for your project, and adapt as necessary (including changing
   `$SCALABHA` to your project name, etc).

Good luck!

# Questions or suggestions?

Email Jason Baldridge: <jasonbaldridge@gmail.com>

Or, create an issue: <https://github.com/utcompling/Scalabha/issues>


