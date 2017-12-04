package service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class CompressorClientSpec extends TestKit(ActorSystem("RLECompressorSpec"))
  with ImplicitSender
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "RLECompressor" should "get index of compressed runs" in {
    val compressorClient = system.actorOf(CompressorClient.props())

    compressorClient ! Compress("NNNNAAAANN".toList)

    compressorClient ! GetIndex(0)
    expectMsg(GetIndexResponse(Some('N')))

    compressorClient ! GetIndex(3)
    expectMsg(GetIndexResponse(Some('N')))

    compressorClient ! GetIndex(4)
    expectMsg(GetIndexResponse(Some('A')))

    compressorClient ! GetIndex(6)
    expectMsg(GetIndexResponse(Some('A')))

    compressorClient ! GetIndex(8)
    expectMsg(GetIndexResponse(Some('N')))

    compressorClient ! GetIndex(9)
    expectMsg(GetIndexResponse(Some('N')))
  }

  it should "not get no existing index of compressed runs" in {
    val compressorClient = system.actorOf(CompressorClient.props())

    compressorClient ! Compress("NNNNAAAA".toList)
    compressorClient ! GetIndex(8)
    expectMsg(GetIndexResponse(None))

    compressorClient ! Compress("NNNNAAAANN".toList)
    compressorClient ! GetIndex(10)
    expectMsg(GetIndexResponse(None))
  }

  it should "update the compressed runs" in {
    val compressorClient = system.actorOf(CompressorClient.props())

    compressorClient ! Compress("NNNNAAAANN".toList)
    compressorClient ! Compress("NNNNAAAA".toList)

    compressorClient ! GetIndex(0)
    expectMsg(GetIndexResponse(Some('N')))

    compressorClient ! GetIndex(3)
    expectMsg(GetIndexResponse(Some('N')))

    compressorClient ! GetIndex(6)
    expectMsg(GetIndexResponse(Some('A')))

    compressorClient ! GetIndex(7)
    expectMsg(GetIndexResponse(Some('A')))
  }

}
