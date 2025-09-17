package za.co.absa.db.fadb.utils

import za.co.absa.db.fadb.naming.NamingConvention
import za.co.absa.db.fadb.naming.implementations.SnakeCaseNaming

import java.lang
import scala.reflect.runtime.universe._

object ClassFieldNamesExtractor {

  private def doExtract[T: TypeTag](namingConvention: NamingConvention): Seq[String] = {
    val tpe = typeOf[T]
    if (tpe.typeSymbol.isClass) {
      val cl = tpe.typeSymbol.asClass
      if (cl.isPrimitive) {
        throw new IllegalArgumentException(s"${tpe.typeSymbol} is a primitive type, extraction is not supported")
      }
      if (cl.isTrait) {
        throw new IllegalArgumentException(s"${tpe.typeSymbol} is a trait, extraction is not supported")
      }
      if (cl.isCaseClass || cl.isClass) {
        tpe
          .decl(termNames.CONSTRUCTOR)
          .asMethod
          .paramLists
          .flatten
          .map(_.name.decodedName.toString)
          .map(namingConvention.stringPerConvention)
    } else {
        throw new IllegalArgumentException(s"${tpe.typeSymbol} is not a case class nor a class")
      }
    } else {
      throw new IllegalArgumentException(s"${tpe.typeSymbol} is not a case class nor a class")
    }
  }

  /**
   * Extracts constructor field names from case class or regular class, and converts them according to naming convention.
   * @param namingConvention - the naming convention to use when converting the constructor parameters names into field name
   * @tparam T               - type to investigate and extract field names from
   * @return                 - list of field names
   */
  def extract[T: TypeTag]()(
                           implicit namingConvention: NamingConvention = SnakeCaseNaming.Implicits.namingConvention
                         ): Seq[String] = {
    doExtract[T](namingConvention)
  }

  def extract[T: TypeTag](namingConvention: NamingConvention): Seq[String] = {
    doExtract[T](namingConvention)
  }

}
