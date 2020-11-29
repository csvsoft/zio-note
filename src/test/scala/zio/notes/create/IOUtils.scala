package zio.notes.create

import java.io.{InputStream, OutputStream}



object IOUtils {

  def copyStream(in:InputStream, out:OutputStream):Unit = {
    val bytes = new Array[Byte](1024)
    var count = 0
    while({count = in.read(bytes);count} != -1){
      out.write(bytes,0,count)
    }
  }
  def getText(in:InputStream):String ={
    scala.io.Source.fromInputStream(in).mkString
  }
}
