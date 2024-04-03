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
    *  @tparam R the type of the data
    */
  case class FunctionStatusWithData[R](functionStatus: FunctionStatus, data: R)

  /**
    *  This is a representation of a single row returned from a DB function with processed status information.
    *
    *  Note: R here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type ExceptionOrStatusWithDataRow[R] = Either[StatusException, FunctionStatusWithData[R]]

  /**
    * This is a representation of multiple rows returned from a DB function with processed status information,
    * with error statuses aggregated to a single one.
    *
    * Note: R here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type ExceptionOrStatusWithDataResultAgg[R] = Either[StatusException, Seq[FunctionStatusWithData[R]]]
}
