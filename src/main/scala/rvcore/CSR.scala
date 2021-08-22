package rvcore

import chisel3._

class CSR {
  val mepc = UInt(32.W) //for jmp pc
  val mcause= UInt(32.W) //exception kind
  val mstatus = UInt(32.W) //save the global ena interrupt
  val mtval   = UInt(32.W) //save the extra info of trap


  //set(& |) wrt
  def access(isCsr:Bool,addr:UInt, pc:UInt, cmd:UInt): Unit = {

  }

  //jmp
  def jmp(isCsr: Bool, addr: UInt, pc: UInt, cmd: UInt): BranchIO = {
    val bru = new BranchIO

    bru
  }
}
