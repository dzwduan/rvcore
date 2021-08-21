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
  val fuType = io.in.ctrl.fuType.asUInt()
  val fuopType = io.in.ctrl.fuOpType.asUInt()

  io.out.data :=DontCare

  when(fuType === FuAlu) {
    io.out.data.dest := ALU(src1,src2,fuopType)
  }.elsewhen(fuType === FuBru) {
    io.out.data.dest := io.in.pc+4.U
  }.elsewhen(fuType === FuLsu) {
    io.out.data.dest := io.dmem.in.rdata
  }.otherwise{
    io.out.data.dest := 0.U
  }


  val lsu = new LSU
  io.dmem <> lsu.access(fuType===FuLsu,src1,src2,fuopType,io.in.data.dest)

  val bru = new BRU
  io.br <> bru.access(fuType===FuBru,io.in.pc,src2,src1,io.in.data.dest,fuopType)


  io.out.ctrl := DontCare
  (io.out.ctrl, io.in.ctrl) match { case (o, i) =>
    o.rfWen := i.rfWen
    o.rfDest := i.rfDest
  }
  io.out.pc := io.in.pc

  printf("EXU: pc = 0x%x , offset = 0x%x \n", io.in.pc, src2)
}
