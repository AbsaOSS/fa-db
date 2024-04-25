package za.co.absa.fadb

import za.co.absa.fadb.exceptions.StatusException

package object status {

  /**
    *  Class represents the status of calling a fa-db function (if it supports status that is)
    */
  case class FunctionStatus(statusCode: Int, statusText: String)

  /**
    *  Represents a function status with data.
    *  @param functionStatus the function status
    *  @param data the data of one row (barring the status fields)
    *  @tparam D the type of the data
    */
  case class Row[D](functionStatus: FunctionStatus, data: D)

  /**
    *  This is a representation of a single row returned from a DB function with processed status information.
    *
    *  Note: D here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type FailedOrRow[D] = Either[StatusException, Row[D]]

  /**
    * This is a representation of multiple rows returned from a DB function with processed status information,
    * with error statuses aggregated to a single one.
    *
    * Note: D here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type FailedOrRowSet[D] = Either[StatusException, Seq[Row[D]]]
}
