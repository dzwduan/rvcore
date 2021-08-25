package hohai

import chisel3._
import Constants._

class IDUIO extends Bundle{
  val inst = Input(UInt(32.W))
  val pc   = Input(UInt(32.W))
  val ctrl_data  = new CtrlDataIO
}


class Decode extends Module {
  val io = IO(new IDUIO)

  val ctrl_data = io.ctrl_data
  val inst = io.inst

  val instValid::fuType::fuOpType::wSel::rs1Type::rs2Type::wen::immType::Nil := CtrlTable

  //data
  ctrl_data.inst := io.inst
  ctrl_data.pc   := io.pc
  ctrl_data.rs1_addr := io.inst(19,15)
  ctrl_data.rs2_addr := io.inst(24,20)
  ctrl_data.rd_addr  := io.inst(11,7)


  val Iimm = inst(31, 20)
  val Simm = Cat(inst(31, 25), inst(11,7))
  val Bimm = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W))
  val Uimm = Cat(inst(31, 12), 0.U(12.W))
  val Jimm = Cat(inst(31), inst(19, 12), inst(20), inst(30, 25), inst(24, 21), 0.U(1.W))
  val ShamtImm = inst(24,20)
  val Zimm = Cat(Fill(27,0.U(1.W)),inst(19,15))

  ctrl_data.nextPC := io.pc + 4.U


  //control
  ctrl_data.rs1Type := rs1Type
  ctrl_data.rs2Type := rs2Type
  ctrl_data.instValid := instValid
  ctrl_data.fuType := fuType
  ctrl_data.fuoptype := fuOpType
  ctrl_data.wen  := wen 
  ctrl_data.immType  := immType
  ctrl_data.w_sel := wSel

  ctrl_data.imm := MuxLookup(inst_type, sext(xlen,Iimm) ,
      Seq(
        IMM_I -> sext(xlen,Iimm),
        IMM_S -> sext(xlen,Simm),
        IMM_B -> sext(xlen,Bimm),
        IMM_U -> sext(xlen,Uimm),
        IMM_J -> sext(xlen,Jimm),
        IMM_SHAMT -> sext(xlen,ShamtImm),
        IMM_Z -> sext(xlen,Zimm)
      )
    ).asUInt

  
  //TODO:if unvalid
}