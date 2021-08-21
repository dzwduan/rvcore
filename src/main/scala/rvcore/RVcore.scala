package rvcore
import chisel3._

class RVcore extends Module{
  val io = IO(new Bundle{
    val imem = new MemIO
    val dmem = new MemIO
    val trap = Output(UInt(2.W))
  })

  val ifu = Module(new IFU)
  val idu = Module(new IDU)
  val isu = Module(new ISU)
  val exu = Module(new EXU)
  val wbu = Module(new WBU)

  io.imem   <> ifu.io.imem
  idu.io.in <> ifu.io.out
  isu.io.in <> idu.io.out
  io.dmem   <> exu.io.dmem
  exu.io.in <> isu.io.out
  wbu.io.in <> exu.io.out
  exu.io.br <> wbu.io.brin
  isu.io.wb <> wbu.io.wb
  ifu.io.br <> wbu.io.brout
  io.trap   := isu.io.trap
}
