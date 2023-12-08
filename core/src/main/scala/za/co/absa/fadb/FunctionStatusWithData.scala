package za.co.absa.fadb

import za.co.absa.fadb.status.FunctionStatus

/**
 * Represents a function status with data.
 * @param functionStatus the function status
 * @param data the data
 * @tparam A the type of the data
 */
case class FunctionStatusWithData[A](functionStatus: FunctionStatus, data: A)
