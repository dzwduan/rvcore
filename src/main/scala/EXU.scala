package hohai

import chisel3._
import Constants.ALU_OP._
import Constants.JMP_OP._

class EXUIO extends Bundle{
  val rs1_data  = Input(64.W)
  val rs2_data  = Input(64.W)
  val ans_data  = Output(64.W)
  val ctrl_data = Flipped(new CtrlDataIO)
  val jmpSel   = Output(Bool())
  val nextPC    = Output(UInt(32.W))
}

class EXU extends Module {
  val io = IO(new EXUIO)

  val ctrl_data = io.ctrl_data 

  val data1:UInt := MuxLookup(io.ctrl_data.rs1Type, 0.U, Array(
    RS_REG -> io.rs1_data,
    RS_PC  -> ctrl_data.pc,
    RS_NPC -> ctrl_data.npc
  )).asUInt

  val data2:UInt := MuxLookup(io.ctrl_data.rs2Type, 0.U, Array(
    RS_REG -> io.rs2_data,
    RS_IMM -> ctrl_data.imm
  )).asUInt


  val shamt = data2(4,0).asUInt
  val imm   = ctrl_data.imm
  val pc    = ctrl_data.pc
  val npc   = ctrl_data.nextPC

  val alu_ans   = Wire(UInt(64.W))
  val JmpSel    = Wire(Bool())
  val JmpTarget = Wire(UInt(64.W))
  val JmpRdData = Wire(UInt(64.W))
  val ld_ans    = Wire(UInt(64.W))



when(ctrl_data.fuType===ALU_T) {
  alu_ans = MuxLookup(ctrl_data.fuOpType, 0.U, Array(
    ADD -> (data1 + data2).asUInt,
    SLL -> (data1<<shamt)(63,0).asUInt,
    SLT -> (data1.asUInt < data2.asUInt).asUInt
    SLTU -> (data1 < data2).asUInt,
    AND -> (data1 & data2).asUInt,
    XOR -> (data1 ^ data2).asUInt,
    SRL -> (data1.asSInt>>shamt).asUInt,
    OR  -> (data1 | data2).asUInt,
    SUB -> (data1 - data2).asUInt,
    SRA -> (data1.asUInt>>shamt).asUInt
  ))
  }

when(ctrl_data.fuType===JMP_T) {
  JmpSel = MuxLookup(ctrl_data.fuOpType,false.B, Array(
    BNE -> (data1 =/= data2),
    BEQ -> (data1 === data2),
    BLT -> (data1.asSInt < data2.asSInt),
    BGE -> (data1.asSInt >= data2.asSInt),
    BLTU -> (data1 < data2),
    BGEU -> (data1 >= data2).
    JAL -> true.B,
    JALR -> true.B
  ))

  JmpTarget = Mux(ctrl_data.fuOpType===JALR, (data1 + imm)&~1, (pc+imm))
  JmpRdData = Wire(UInt(64.W))
  when(ctrl_data.fuOpType===JALR || ctrl_data.fuOpType===JAL){
    JmpRdData := zext(64,npc)
  }
}



  
  //ld x[rd] = M[x[rs1] + sext(offset)][63:0]          
  //st M[x[rs1] + sext(offset) = x[rs2][15:0]    
  
  when(ctrl_data.fuType===LSU_T) {
  //load/store
  //是否需要提到外面？
    val dmem = Mem(4096, UInt(8.W))
    val ld_addr = data1 + imm 
    //each byte add related sel signal
    val ld_data := Cat(
                    dmem(ld_addr+7.U),dmem(ld_addr+6.U),
                    dmem(ld_addr+5.U),dmem(ld_addr+4.U),
                    dmem(ld_addr+3.U),dmem(ld_addr+2.U),
                    dmem(ld_addr+1.U),dmem(ld_addr+0.U))
    
    ld_ans  := MuxLookup(ctrl_data.fuOpType,0.U,Array(
      MEM_LB   -> sext(xlen,ld_data(7,0)),
      MEM_LH   -> sext(xeln,ld_data(15,0)),
      MEM_LW   -> sext(xlen,ld_data(31,0)),
      MEM_LD   -> ld_data,
      MEM_LBU  -> zext(xlen,ld_data(7,0)),
      MEM_LHU  -> zext(xlen,ld_data(15,0)),
      MEM_LW   -> zext(xlen,ld_data(31,0)),
      MEM_LD   -> ld_data
    ))

    //store init
    switch(ctrl_data.fuOpType) {
      is MEM_SB{
        dmem(ls_addr) := data2(7,0)
      }
      is MEM_SH{
        dmem(ls_addr+1.U) := data2(15,8)
        dmem(ls_addr) := data2(7,0)
      }
      is MEM_SW{
        dmem(ls_addr+3.U) := data2(31,25)
        dmem(ls_addr+2.U) := data2(24,16)
        dmem(ls_addr+1.U) := data2(15,8)
        dmem(ls_addr+0.U) := data2(7,0)
      }
      is MEM_SD{
        dmem(ls_addr+7.U) := data2(63,56)
        dmem(ls_addr+6.U) := data2(55,48)
        dmem(ls_addr+5.U) := data2(47,40)
        dmem(ls_addr+4.U) := data2(39,32)
        dmem(ls_addr+3.U) := data2(31,25)
        dmem(ls_addr+2.U) := data2(24,16)
        dmem(ls_addr+1.U) := data2(15,8)
        dmem(ls_addr+0.U) := data2(7,0)
      }
    }
  }


  io.ans_data := alu_ans | JmpRdData | ld_ans
  io.jmpSel := JmpSel
  io.nextPC := Mux(JmpSel, JmpTarget, ctrl_data.nextPC)
}
