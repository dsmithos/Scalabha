package opennlp.scalabha.preproc

import scala.xml._
import org.clapper.argot.ArgotParser._
import opennlp.scalabha.log.SimpleLogger
import org.clapper.argot.{ArgotUsageException, ArgotParser, ArgotConverters}
import java.io._
import org.xml.sax.SAXParseException
import ArgotConverters._
import opennlp.scalabha.model.{Value, Node}
import com.sun.org.apache.xpath.internal.operations.Mult
import opennlp.scalabha.tree.MultiLineTreeParser

object TOK2TREE {
  val parser = new ArgotParser(this.getClass.getName, preUsage = Some("Version 0.0"))
  val help = parser.flag[Boolean](List("h", "help"), "print help")
  val inputOpt = parser.option[String](List("i", "inputTokens"), "FILE_OR_DIR", "Input inputFile or directory to tokenize")
  val outputOpt = parser.option[String](List("o", "outputTrees"), "DIR", "Output location for the tree files. " +
    "Each tree gets its own file, and they are named from the input file.")

  val debug = parser.flag[Boolean](List("d", "debug"), "Assert this flag if you want to see ridicuous quantities of output.")

  var log: SimpleLogger = new SimpleLogger(
    this.getClass.getName,
    SimpleLogger.WARN,
    new BufferedWriter(new OutputStreamWriter(System.err)))

  val tagDictionary = Map(
    ("." -> "."),
    ("," -> ","),
    ("..." -> "..."),
    ("?" -> "?"),
    ("!" -> "!")
  ).withDefaultValue("x")

  def getTree(tokLine: String): Node = 
    Node("TOP",
      tokLine
        .replaceAll("\\(", "-LRB-")
        .replaceAll("\\)", "-RRB-")
        .split("<EOS>")
        .map(s=>s.trim)
        .filter(s => s.length > 0)
        .map(sentence => Node("S", sentence.split("\\s+").map(word => Node(tagDictionary(word), List[Value](Value(word)))).toList))
        .toList
    )
  
  def getFormattedString(tokLine: String): String = getTree(tokLine).getCanonicalString().replaceAll("\\s*\\(S","\n    (S")
  
  /**
   * Build a rudimentary syntax tree from a tokenized line.
   * @param tokLine A space-separated list of tokens
   * @return a string representation of a syntax tree.
   */
  def apply(tokLine: String): String = getFormattedString(tokLine)

  /**
   * A file is ok to overwrite if it does not exist, or it is an autogenerated file, which we
   * can tell from the structure.
   */
  def okToWrite(file: File): Boolean = {
    val okNotExist = !file.exists()
    val okBoilerplate =
      (file.canWrite && MultiLineTreeParser(file.getPath).filter {
        (treeNode) => {
          // list of only treenodes that are not ok to overwrite.
          val autoGenNotOk = (treeNode.getTagCounts().filter {
            case (tag, count) => tag != "TOP" && tag != "S" && tag != "x"
          }.size != 0)
          val depthNotOk = (treeNode.getHeight() != 3)
          autoGenNotOk || depthNotOk
        }
      }.length == 0)
    okNotExist || okBoilerplate
  }

  /**
   * Transform a token file into a directory of rudimentary tree file.
   * @param inputfile A file consisting of lines of tokenized text, with sentences delimited by <EOS> tags
   * @param treeDir The directory to write trees to. Each tree (corresponding to a line in the token file)
   * gets its own file.
   * @return Nothing. The output is written to treeDir.
   */
  def apply(inputFile: File, treeDir: File) {
    log.debug("Started file transform in:%s out:%s\n".format(inputFile.getPath, treeDir.getPath))
    assert(inputFile.isFile, "input file is not a file.")
    assert(inputFile.getName.endsWith(".tok"))
    val baseName = inputFile.getName.substring(0, inputFile.getName.length() - 4)
    log.debug("Making parent directories and text file\n")
    treeDir.mkdirs()
    log.info("%s -> %s/%s.{tree#...}.tree\n".format(inputFile.getAbsolutePath, treeDir.getAbsolutePath, baseName))

    // I'm reading the whole input file on purpose, since we're dong a lot of small write jobs,
    // I don't want to waste time reading in sub-file chunks.
    val lines = scala.io.Source.fromFile(inputFile, "UTF-8").getLines().toList
    val width = math.log10(lines.length).toInt + 1
    for ((line, i) <- lines.zipWithIndex) {
      val index = i + 1
      val outputFile = new File(treeDir, ("%s.%0" + width + "d.tree").format(baseName, index))
      if (okToWrite(outputFile)) {
        log.trace("Writing %s.\n".format(outputFile.getPath))
        val writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
        val treeString = apply(line)
        writer.write(treeString + "\n")
        writer.close()
      } else {
        log.warn(("File %s: This file looks like it's been modified." +
          " Delete it and re-run this program if you want to overwrite it. Skipping...\n").format(outputFile.getPath))
      }
    }
  }

  /**
   * Descend a directory structure looking for token files, and recreate the same directory structure
   * with tree files, re-rooted at treeDir
   */
  def applyDir(inputDir: File, treeDir: File) {
    assert(inputDir.isDirectory)
    for (child <- inputDir.listFiles().sorted) {
      if (child.isDirectory) {
        val pathDescentStep = child.getName
        applyDir(child, new File(treeDir, pathDescentStep))
      } else if (child.isFile && child.getName.endsWith(".tok")) {
        apply(child, new File(treeDir, child.getName.substring(0, child.getName.length() - 4)))
      }
    }
  }

  def main(args: Array[String]) {
    var warnings = 0
    var errors = 0
    try {
      parser.parse(args)

      if (help.value.isDefined) {
        parser.usage()
      }
      if (debug.value.isDefined) {
        log.logLevel = SimpleLogger.DEBUG
      }
      MultiLineTreeParser.log.logLevel = log.logLevel
      val inputFile = inputOpt.value match {
        case Some(filename) => new File(filename).getAbsoluteFile
        case None => parser.usage("You must specify an input file")
      }
      val textFile = outputOpt.value match {
        case Some(filename) => new File(filename)
        case None => parser.usage("You must specify a text file")
      }
      if (inputFile.isFile) {
        apply(inputFile, textFile)
      } else if (inputFile.isDirectory) {
        applyDir(inputFile, textFile)
      } else {
        parser.usage("input file must be a regular file")
      }
      val (transformWarnings, transformErrors) = log.getStats()
      warnings = transformWarnings
      errors = transformErrors
      log.summary("Warnings,Errors: %s\n".format((warnings, errors)))
    }
    catch {
      case e: ArgotUsageException =>
        println(e.message)
    }
    System.exit(errors)
  }

}
