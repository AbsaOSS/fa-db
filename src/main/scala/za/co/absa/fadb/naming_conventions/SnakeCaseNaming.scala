/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.fadb.naming_conventions

import za.co.absa.fadb.naming_conventions.lettersCase.LettersCase
import za.co.absa.fadb.naming_conventions.lettersCase.LettersCase.LowerCase

class SnakeCaseNaming(lettersCase: LettersCase) extends NamingConvention {
  private def camelCaseToSnakeCase(s: String): String = {
    s.replaceAll("([A-Z])", "_$1")
  }

  private def stripIfFirstChar(s: String, ch: Char): String = {
    if (s == "") {
      s
    } else if (s(0) == ch){
      s.substring(1)
    } else {
      s
    }
  }

  override def stringPerConvention(original: String): String = {
    lettersCase.convert(
    stripIfFirstChar(
    camelCaseToSnakeCase(original), '_'))
  }
}

object SnakeCaseNaming {
  object Implicits {
    implicit val namingConvention: NamingConvention = new SnakeCaseNaming(LowerCase)
  }
}
