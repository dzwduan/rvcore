package core

import chisel3._
import chisel3.util._

import global_config._

class CtrlPathIO extends Bundle {
  val src1Type = Output(UInt(Src1TypeWidth))
  val src2Type = Output(UInt(Src2TypeWidth))
  val fuType = Output(UInt(FuTypeWidth))
  val fuOpType = Output(UInt(FuOpTypeWidth))
  val rfSrc1 = Output(UInt(5.W))
  val rfSrc2 = Output(UInt(5.W))
  val rfWen = Output(Bool())
  val rfDest = Output(UInt(5.W))
  val isTrap = Output(UInt(2.W))
}

class DataPathIO extends Bundle {
  val src1 = Output(UInt(32.W))
  val src2 = Output(UInt(32.W))
  val dest = Output(UInt(32.W))
}

class PcInstrIO extends Bundle {
  val instr = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
}

class PcCtrlDataIO extends Bundle {
  val pc = Output(UInt(32.W))
  val ctrl = new CtrlPathIO
  val data = new DataPathIO
}

class WriteBackIO extends Bundle {
  val rfWen = Output(Bool())
  val rfDest = Output(UInt(5.W))
  val rfWdata = Output(UInt(32.W))
}


class ABundle extends Bundle {
  val addr = Output(UInt(32.W))
  val wdata = Output(UInt(32.W))
  val wen = Output(Bool())
}

class RBundle extends Bundle {
  val rdata = Output(UInt(32.W))
}

class MemIO extends Bundle {
  val out = Valid(new ABundle)
  val in = Flipped(new RBundle)
}