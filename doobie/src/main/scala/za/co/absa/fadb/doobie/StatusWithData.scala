package za.co.absa.fadb.doobie

/**
 *  Represents a function status with data.
 */
case class StatusWithData[R](status: Int, status_text: String, data: R)
