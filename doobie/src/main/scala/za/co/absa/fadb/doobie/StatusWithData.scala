package za.co.absa.fadb.doobie

/**
 * Represents a function status with data.
 * @param functionStatus the function status
 * @param data the data
 * @tparam A the type of the data
 */
case class StatusWithData[R](status: Int, status_text: String, data: R)
