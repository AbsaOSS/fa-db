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

package za.co.absa.fadb.slick

import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.Query

/**
  * SQL query representation for Slick
  * @param sql        - the SQL query in Slick format
  * @param getResult  - the converting function, that converts the [[slick.jdbc.PositionedResult slick.PositionedResult]] (the result of Slick
  *                   execution) into the desire `R` type
  * @tparam R         - the return type of the query
  */
class SlickQuery[R](val sql: SQLActionBuilder, val getResult: GetResult[R]) extends Query[R]
