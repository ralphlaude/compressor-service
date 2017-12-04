package service

import org.scalatest.{FlatSpec, Matchers}


class CompressorSpec extends FlatSpec with Matchers {

  /** Test object for the compressor */
  private case object TestCompressor extends Compressor


  "RLE compression" should "compress NNNN" in {
    val compression = TestCompressor.compress.apply("NNNN".toList)
    compression shouldBe Seq(Repeat(4, 'N'))
  }

  it should "compress NNNNAAAA" in {
    val compression = TestCompressor.compress.apply("NNNNAAAA".toList)
    compression shouldBe Seq(Repeat(4, 'N'), Repeat(4, 'A'))
  }

  it should "compress NNNNAAAANN" in {
    val compression = TestCompressor.compress.apply("NNNNAAAANN".toList)
    compression shouldBe Seq(Repeat(4, 'N'), Repeat(4, 'A'), Repeat(2, 'N'))
  }

  it should "compress NNNNAAAAN" in {
    val compression = TestCompressor.compress.apply("NNNNAAAAN".toList)
    compression shouldBe Seq(Repeat(4, 'N'), Repeat(4, 'A'), Repeat(1, 'N'))
  }

  it should "compress NAAAAN" in {
    val compression = TestCompressor.compress.apply("NAAAAN".toList)
    compression shouldBe Seq(Repeat(1, 'N'), Repeat(4, 'A'), Repeat(1, 'N'))
  }

}

