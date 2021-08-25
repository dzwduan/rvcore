package hohai

import chisel3._

trait Constants {
  val ALU_ADDI = 1.U(6.W)
}

class CtrlDataIO extends Bundle{
  //ctrl part
  val rs1Type   = Output(UInt(2.W))
  val rs2Type   = Output(UInt(2.W))
  val instValid = Output(Bool())
  val fuType    = Output(UInt(4.W))
  val fuOpType  = Output(UInt(4.W))
  val rd_wen    = Output(Bool())
  val w_sel     = Output(Bool())
  //data part
  val pc        = Output(UInt(32.W))
  val imm       = Output(UInt(64.W))
  val inst      = Output(UInt(32.W))
  val rs1Addr   = Output(UInt(64.W))
  val rs2Addr   = Output(UInt(64.W))
  val rdAddr    = Output(UInt(64.W))
  val nextPC    = Output(UInt(32.W))
} 