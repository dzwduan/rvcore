package core

import chisel3._

class Reg{
  val regfiles = Mem(32,UInt(32.W))
  def read(addr:UInt) = {
    Mux(addr===0.U,0.U,regfiles(addr))
  }

  def write(addr:UInt,data:UInt) = {
    regfiles(addr) := data
  }
}


class ISUIO extends Bundle{
  val in = Flipped(new PcCtrlDataIO)
  val out = new PcCtrlDataIO
  val wb = Flipped(new WriteBackIO)
  val trap = Output(UInt(2.W))
}

//input pc ctrl data wb, output pc ctrl data

class ISU extends Module{
  val io = IO(new ISUIO)

  val rf = new Reg
  val rs1data = rf.read(io.in.ctrl.rfSrc1)
  val rs2data = rf.read(io.in.ctrl.rfSrc2)
  io.out.data.src1 := Mux(io.in.ctrl.src1Type.asBool(),io.in.pc,rs1data)
  io.out.data.src2 := Mux(io.in.ctrl.src2Type.asBool(),rs2data,io.in.data.src2)

  io.out.data.dest := DontCare

  when(io.wb.rfWen) {
    rf.write(io.wb.rfDest,io.wb.rfWdata)
  }

  io.out.pc := io.in.pc
  io.out.ctrl <> io.in.ctrl

  when(io.in.ctrl.isTrap(1) === 1.U){
    io.trap := 2.U
  }.elsewhen(io.in.ctrl.isTrap === 1.U && rs1data === 0.U){
    io.trap := 0.U
  }.elsewhen(io.in.ctrl.isTrap === 1.U && rs1data =/= 0.U){
    io.trap := 1.U
  }.otherwise{
    io.trap := 3.U
  }
}
