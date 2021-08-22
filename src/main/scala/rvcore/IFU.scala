package rvcore

import chisel3._
import global_config._


//initialize imem and output pc with instr

trait pc_init {
  val pc_init   = 0x80000000L
  val pc_shift  = 0x7f000000L
  //  val pc_init   = 0x01000000L
  //  val pc_shift  = 0x00000000L
  def addrMap(src: Long) : Long = src - pc_shift
  def addrMap(src: BigInt): BigInt = src - BigInt(pc_shift)
}


class IFUIO extends Bundle{
  val imem = new MemIO
  val out  = new PcInstrIO
  val br   = Flipped(new BranchIO)
}

class IFU extends Module with pc_init {
  val io = IO(new IFUIO)

  val pc = RegInit(pc_init.U(addr_width))

  pc := Mux(io.br.isTaken,io.br.target,pc+4.U)

  io.imem.out.valid    := true.B
  io.imem.out.bits.wen := false.B
  io.imem.out.bits.size := LsuLw
  io.imem.out.bits.addr := pc
  io.imem.out.bits.wdata := DontCare

  io.out.pc := pc
  io.out.instr := io.imem.in.rdata
}
