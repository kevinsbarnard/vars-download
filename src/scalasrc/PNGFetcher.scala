package scalasrc

import java.io.File
import java.net.{MalformedURLException, URL}
import java.nio.file.Files

import scala.util.{Failure, Success, Try}

/**
  * PNG fetching helper object
  *
  * @author Brian Schlining
  * @author Kevin Barnard
  */
object PNGFetcher {

  def extractPngURL(jpg: String): String = {
    val lowerJpg = jpg.toLowerCase()
    if (lowerJpg.startsWith("http") && lowerJpg.endsWith(".jpg")) {
      jpg.replace(".jpg", ".png")
    } else if (lowerJpg.lastIndexOf('.') < lowerJpg.length - 4) {
      jpg + ".png"
    } else "VARS ENTRY ERROR"
  }

  @throws(classOf[MalformedURLException])
  def urlToFile(baseDir: File, pngUrl: String, rovName: String, diveNumber: String): File = {
    val url = new URL(pngUrl)
    val pngBasename = url.toExternalForm
      .replace("%20", " ")
      .replace(":", "-")
      .split("/")
      .last
    val pngName = s"${rovName}_${diveNumber}_$pngBasename"
    new File(baseDir, pngName)
  }

  def save(url: URL, target: File): Unit = {
    print(s"Copying $url to $target")

    // capture Try's value or my strict compiler settings will barf
    val foo = Try {
      val in = url.openStream()
      val parent = target.getParentFile
      if (!parent.exists()) target.mkdirs()
      Files.copy(in, target.toPath)
      in.close()
    } match {
      case Success(_) => print(" ... DONE\n")
      case Failure(e) => {
        println(s" ... FAILED: $e")
        if (target.exists()) target.delete()
      }
    }

  }

}
