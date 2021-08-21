package rvcore

import chisel3._
import chisel3.util._
import global_config._


class EXUIO extends Bundle{
  val in   = Flipped(new PcCtrlDataIO)
  val out  = new PcCtrlDataIO
  val br   = new BranchIO
  val dmem = new MemIO
}

class EXU extends Module{
  val io = IO(new EXUIO)

  val src1 = io.in.data.src1.asUInt()
  val src2 = io.in.data.src2.asUInt()
  val func = io.in.ctrl.fuOpType.asUInt()
  val fuType = io.in.ctrl.fuType.asUInt()

  io.out.data :=DontCare

  when(fuType === FuAlu) {
    io.out.data.dest := ALU(src1,src2,func)
  }.elsewhen(fuType === FuBru) {
    io.out.data.dest := io.in.pc+4.U
  }.elsewhen(fuType === FuLsu) {
    io.out.data.dest := io.dmem.in.rdata
  }.otherwise{
    io.out.data.dest := 0.U
  }


  val (isTaken,target) = (new BRU).apply(src1,src2,func)
  val (wen,addr) = LSU(src1,src2,func)

  io.br.isTaken := isTaken
  io.br.target := target
  io.dmem.out.bits.addr := addr
  io.dmem.out.bits.wen  := wen && (io.in.ctrl.fuType===FuLsu)
  io.dmem.out.bits.wdata := io.in.data.dest
  io.dmem.out.valid := fuType === FuLsu

  io.out.ctrl := DontCare
  (io.out.ctrl, io.in.ctrl) match { case (o, i) =>
    o.rfWen := i.rfWen
    o.rfDest := i.rfDest
  }
  io.out.pc := io.in.pc
}
