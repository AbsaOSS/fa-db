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

import com.github.tminglei.slickpg._
import za.co.absa.fadb.slick.support.PgUUIDSupport

/**
 *  DB profile recommended to use with SlickPgEngine to offer support for all extended Postgres types.
 *  JSON is not included, as they are multiple JSON implementations. Choose the one of your liking and extend
 *  [[FaDbPostgresProfile]] with it. More on [SlickPG](https://github.com/tminglei/slick-pg/tree/master) page.
 */
trait FaDbPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgNetSupport
    with PgRangeSupport
    with PgLTreeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgUUIDSupport {

  trait FaDbAPI
      extends super.API
      with ArrayImplicits
      with Date2DateTimeImplicitsDuration
      with NetImplicits
      with RangeImplicits
      with LTreeImplicits
      with HStoreImplicits
      with SimpleArrayPlainImplicits
      with Date2DateTimePlainImplicits
      with SimpleNetPlainImplicits
      with SimpleRangePlainImplicits
      with SimpleLTreePlainImplicits
      with SimpleHStorePlainImplicits
      with UUIDPlainImplicits

  override val api: FaDbAPI = new FaDbAPI {}
}

object FaDbPostgresProfile extends FaDbPostgresProfile
