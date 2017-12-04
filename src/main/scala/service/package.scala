package object service {

  case class Compress(runs: List[Char])
  case class GetIndex(index: Int)
  case class GetIndexResponse(ch: Option[Char])

}
