package za.co.absa.fadb

import za.co.absa.fadb.status.FunctionStatus

case class FunctionStatusWithData[A] (functionStatus: FunctionStatus, data: A)
