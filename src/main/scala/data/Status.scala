package data

sealed abstract class Status
case object Uncompleted extends Status

sealed abstract class PostrunStatus extends Status
case object Completed extends PostrunStatus
case object Failed extends PostrunStatus