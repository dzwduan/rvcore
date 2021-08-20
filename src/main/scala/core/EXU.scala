package core

import chisel3._
import chisel3.util._
import global_config._


class EXUIO extends Bundle{
  val in = Flipped(new PcCtrlDataIO)
  val out = new PcCtrlDataIO
}

class EXU extends Module{
  val io = IO(new EXUIO)

  val src1 = io.in.data.src1.asUInt()
  val src2 = io.in.data.src2.asUInt()
  val func = io.in.ctrl.fuOpType.asUInt()

  io.in<>io.out
  io.out.data.dest := Mux(io.in.ctrl.fuType === FuAlu,ALU(src1,src2,func),0.U)
}
