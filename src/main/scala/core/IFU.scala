package core

import chisel3._


//initialize imem and output pc with instr

trait pc_init {
  val init_val = 0x80000000L
}


class IFUIO extends Bundle{
  val imem = new MemIO
  val out  = new PcInstrIO
}

class IFU extends Module with pc_init {
  val io = IO(new IFUIO)

  val pc = RegInit(init_val.U(64.W))

  pc := pc + 4.U

  io.imem.out.valid := true.B
  io.imem.out.bits.wen := true.B
  io.imem.out.bits.addr := pc
  io.imem.out.bits.wdata := DontCare

  io.out.pc := pc
  io.out.instr := io.imem.in.rdata
}
