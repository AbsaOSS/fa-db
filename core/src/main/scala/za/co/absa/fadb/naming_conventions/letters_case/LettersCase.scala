/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.fadb.naming_conventions.letters_case

sealed trait LettersCase {
  def convert(s: String): String
}

object LettersCase {
  case object AsIs extends LettersCase {
    override def convert(s: String): String = s
  }
  case object LowerCase extends LettersCase {
    override def convert(s: String): String = s.toLowerCase
  }
  case object UpperCase extends LettersCase {
    override def convert(s: String): String = s.toUpperCase
  }
}
