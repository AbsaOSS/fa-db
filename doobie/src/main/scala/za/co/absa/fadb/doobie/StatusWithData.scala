package za.co.absa.fadb.doobie

case class StatusWithData[R](status: Int, status_text: String, data: R)
