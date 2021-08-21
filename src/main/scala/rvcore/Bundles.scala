package rvcore

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
  val src1 = Output(UInt(data_width))
  val src2 = Output(UInt(data_width))
  val dest = Output(UInt(data_width))
}

class PcInstrIO extends Bundle {
  val instr = Output(UInt(inst_width))
  val pc = Output(UInt(addr_width))
}

class PcCtrlDataIO extends Bundle {
  val pc = Output(UInt(addr_width))
  val ctrl = new CtrlPathIO
  val data = new DataPathIO
}

class WriteBackIO extends Bundle {
  val rfWen = Output(Bool())
  val rfDest = Output(UInt(5.W))
  val rfWdata = Output(UInt(data_width))
}


class ABundle extends Bundle {
  val addr = Output(UInt(addr_width))
  val size = Output(UInt(2.W))
  val wdata = Output(UInt(data_width))
  val wen = Output(Bool())
}

class RBundle extends Bundle {
  val rdata = Output(UInt(data_width))
}

class MemIO extends Bundle {
  val out = Valid(new ABundle)
  val in = Flipped(new RBundle)
}

class BranchIO extends Bundle {
  val isTaken = Output(Bool())
  val target = Output(UInt(addr_width))
}