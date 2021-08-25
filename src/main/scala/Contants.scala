import chisel3._ 
import Constants._
import INstructions._
import Constants.FU_TYPE._
import Constants.JMP_OP._
import Constants._
import Constants.LSU_TYPE._


object Constants{
    val Y = true.B 
    val N = false.B 

    def xlen = 64

    object FU_TYPE{
        def ALU_T = "b000".U
        def JMP_T = "b001".U 
        def LSU_T = "b010".U
        def CSR_T = "b011".U 
        def FUX_T = "b111".U
    }
    
    object ALU_OP{
        def ALU_ADD   = "b0000".U
        def ALU_SLL   = "b0001".U 
        def ALU_SLT   = "b0010".U 
        def ALU_SLTU  = "b0011".U 
        def ALU_AND   = "b0100".U
        def ALU_XOR   = "b0101".U 
        def ALU_SRL   = "b0110".U 
        def ALU_OR    = "b0111".U 
        def ALU_SUB   = "b1000".U 
        def ALU_SRA   = "b1001".U
        def ALU_X     = "b1111".U
    }

    object JMP_OP{
        def JMP_BNE  =  "b0000".U 
        def JMP_BEQ  =  "b0001".U 
        def JMP_BLT  =  "b0010".U 
        def JMP_BGE  =  "b0011".U 
        def JMP_BLTU = "b0100".U 
        def JMP_BGEU = "b0101".U
        def JMP_JAL  = "b0110".U 
        def JMP_JALR = "b0111".U 
        def JMP_X    = "b1111".U
    }

    object LSU_TYPE{
        def MEM_LB  =  "b0000".U 
        def MEM_LH  =  "b0001".U 
        def MEM_LW  =  "b0010".U 
        def MEM_LBU = "b0011".U 
        def MEM_LHU = "b0100".U 
        def MEM_SB  =  "b0101".U 
        def MEM_SH  =  "b0110".U 
        def MEM_SW  =  "b0111".U 
        def MEM_LD  = "b1000".U
        def MEM_SD  = "b1001".U 
        def MEM_X   =  "b1111".U
    }


    object RS_TYPE{
        def RS_REG = "b000".U 
        def RS_IMM = "b001".U
        def RS_PC  = "b010".U
        def RS_NPC = "b011".U
        def RS_ZERO= "b100".U
        def RS_X   = "b111".U
    }

    object CSR_TYPE{
        def CSR_RW    = "b000".U 
        def CSR_RS    = "b000".U 
        def CSR_RC    = "b000".U 
        def CSR_ECALL = "b000".U 
        def CSR_MRET  = "b000".U 
        def CSR_X     = "b000".U 
    }

    object IMM_TYPE{
        def IMM_I   = "b000".U 
        def IMM_S   = "b001".U 
        def IMM_B   = "b010".U 
        def IMM_J   = "b011".U 
        def IMM_U   = "b100".U 
        //CSR
        def IMM_Z   = "b101".U 
        def IMM_X   = "b111".U
    }

 
}



val CtrlTable = ListLookup(
    io.inst,
    //                   func          w-type         rs2_type       imm_type
    //              valid \   funoptype  \   rs1_type  \       rd_en  \
    //                \    \      \       \     \       \        \     \
                  List(N, FUX_T, ALU_X,    N, RS_X,     RS_X,    N, IMM_X    ), 
    Array(
      // RV32I
      LUI     ->  List(Y, ALU_T, ALU_ADD,  N, RS_ZERO,  RS_IMM,  Y, IMM_U    ),
      AUIPC   ->  List(Y, ALU_T, ALU_ADD,  N, RS_PC,    RS_IMM,  Y, IMM_U    ),
      // x[rd] = pc+4; pc += sext(offset)
      JAL     ->  List(Y, JMP_T, JMP_JAL   N, RS_PC,    RS_IMM,  Y, IMM_J    ),
      // t=pc+4; pc=(x[rs1]+sext(offset))&~1; x[rd]=t
      JALR    ->  List(Y, JMP_T, JMP_JALR, N, RS_REG,   RS_REG,  Y, IMM_I    ),
      // if (rs1 == rs2) pc += sext(offset)
      BEQ     ->  List(Y, JMP_T, JMP_BEQ,  N, RS_REG,   RS_REG,  N, IMM_B    ),
      BNE     ->  List(Y, JMP_T, JMP_BNE,  N, RS_REG,   RS_REG,  N, IMM_B    ),
      BLT     ->  List(Y, JMP_T, JMP_BLT,  N, RS_REG,   RS_REG,  N, IMM_B    ),
      BGE     ->  List(Y, JMP_T, JMP_BGE,  N, RS_REG,   RS_REG,  N, IMM_B    ),
      BLTU    ->  List(Y, JMP_T, JMP_BLTU, N, RS_REG,   RS_REG,  N, IMM_B    ),
      BGEU    ->  List(Y, JMP_T, JMP_BGEU, N, RS_REG,   RS_REG,  N, IMM_B    ),
      //x[rd] = sext(M[x[rs1] + sext(offset)][7:0])
      LB      ->  List(Y, LSU_T, MEM_LB,   N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      LH      ->  List(Y, LSU_T, MEM_LH    N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      LW      ->  List(Y, LSU_T, MEM_LW    N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      LBU     ->  List(Y, LSU_T, MEM_LBU   N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      LHU     ->  List(Y, LSU_T, MEM_LHU   N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      //M[x[rs1] + sext(offset) = x[rs2][7: 0]
      SB      ->  List(Y, LSU_T, MEM_SB    N, RS_REG,   RS_REG,  N, IMM_S    ),
      SH      ->  List(Y, LSU_T, MEM_SH    N, RS_REG,   RS_REG,  N, IMM_S    ),
      SW      ->  List(Y, LSU_T, MEM_SW    N, RS_REG,   RS_REG,  N, IMM_S    ),
      //x[rd] = x[rs1] + sext(immediate)
      ADDI    ->  List(Y, ALU_T, ALU_ADD,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SLTI    ->  List(Y, ALU_T, ALU_SLT,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SLTIU   ->  List(Y, ALU_T, ALU_SLTU, N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      XORI    ->  List(Y, ALU_T, ALU_XOR,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      ORI     ->  List(Y, ALU_T, ALU_OR,   N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      ANDI    ->  List(Y, ALU_T, ALU_AND,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      //x[rd] = x[rd] << uimm
      SLLI    ->  List(Y, ALU_T, ALU_SLL,  N, RS_REG,   RS_IMM,  Y, IMM_SHAMT),
      //x[8+rd’] = x[8+rd’] >>u uimm
      SRLI    ->  List(Y, ALU_T, ALU_SRL,  N, RS_REG,   RS_IMM,  Y, IMM_SHAMT),
      //x[8+rd’] = x[8+rd’] >>s uimm
      SRAI    ->  List(Y, ALU_T, ALU_SRA,  N, RS_REG,   RS_IMM,  Y, IMM_SHAMT),
      //x[rd] = x[rs1] + x[rs2]
      ADD     ->  List(Y, ALU_T, ALU_ADD,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SUB     ->  List(Y, ALU_T, ALU_SUB,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SLL     ->  List(Y, ALU_T, ALU_SLL,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SLT     ->  List(Y, ALU_T, ALU_SLT,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SLTU    ->  List(Y, ALU_T, ALU_SLTU, N, RS_REG,   RS_REG,  Y, IMM_X    ),
      XOR     ->  List(Y, ALU_T, ALU_XOR,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SRL     ->  List(Y, ALU_T, ALU_SRL,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      SRA     ->  List(Y, ALU_T, ALU_SRA,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      OR      ->  List(Y, ALU_T, ALU_OR,   N, RS_REG,   RS_REG,  Y, IMM_X    ),
      AND     ->  List(Y, ALU_T, ALU_AND,  N, RS_REG,   RS_REG,  Y, IMM_X    ),
      // FENCE
      // FENCE_I
     // RaiseException(EnvironmentCall)
      ECALL   ->  List(Y, CSR_T, CSR_ECALL,N, RS_X,     RS_X,    N, IMM_X    ),
      // EBREAK
      // set pc priv mstatus
      MRET    ->  List(Y, CSR_T, CSR_MRET, N, RS_X,     RS_X,    N, IMM_X    ),
      // while (noInterruptPending) idle
      WFI     ->  List(Y, ALU_T, CSR_X,    N, RS_X,     RS_X,    N, IMM_X    ),
      // RV64I
      //x[rd] = M[x[rs1] + sext(offset)][31:0]
      LWU     ->  List(Y, LSU_T, MEM_LWU,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      LD      ->  List(Y, LSU_T, MEM_LDU,  N, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SD      ->  List(Y, LSU_T, MEM_SD,   N, RS_REG,   RS_REG,  N, IMM_S    ),
      ADDIW   ->  List(Y, ALU_T, ALU_ADD,  Y, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SLLIW   ->  List(Y, ALU_T, ALU_SLL,  Y, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SRLIW   ->  List(Y, ALU_T, ALU_SRL,  Y, RS_REG,   RS_IMM,  Y, IMM_I    ),
      SRAIW   ->  List(Y, ALU_T, ALU_SRA,  Y, RS_REG,   RS_IMM,  Y, IMM_I    ),
      ADDW    ->  List(Y, ALU_T, ALU_ADD,  Y, RS_REG,   RS_REG,  Y, IMM_X    ),
      SUBW    ->  List(Y, ALU_T, ALU_SUB,  Y, RS_REG,   RS_REG,  Y, IMM_X    ),
      SLLW    ->  List(Y, ALU_T, ALU_SLL,  Y, RS_REG,   RS_REG,  Y, IMM_X    ),
      SRLW    ->  List(Y, ALU_T, ALU_SRL,  Y, RS_REG,   RS_REG,  Y, IMM_X    ),
      SRAW    ->  List(Y, ALU_T, ALU_SRA,  Y, RS_REG,   RS_REG,  Y, IMM_X    ),
      // CSR
      //t = CSRs[csr]; CSRs[csr] = x[rs1]; x[rd] = t
      CSRRW   ->  List(Y,CSR_T,  CSR_RW,   N, RS_REG,   RS_X,    Y, IMM_X    ),
      CSRRS   ->  List(Y,CSR_T,  CSR_RS,   N, RS_REG,   RS_X,    Y, IMM_X    ),
      //t = CSRs[csr]; CSRs[csr] = t | x[rs1]; x[rd] = t
      CSRRC   ->  List(Y,CSR_T,  CSR_RC,   N, RS_REG,   RS_X,    Y, IMM_X    ),
      //x[rd] = CSRs[csr]; CSRs[csr] = zimm
      CSRRWI  ->  List(Y,CSR_T,  CSR_RW,   N, RS_IMM,   RS_X,    Y, IMM_Z  ),
      //csrrsi rd, csr, zimm[4:0]    t = CSRs[csr]; CSRs[csr] = t | zimm; x[rd] = t
      CSRRSI  ->  List(Y,CSR_T,  CSR_RS,   N, RS_IMM,   RS_X,    Y, IMM_Z  ),
      //t = CSRs[csr]; CSRs[csr] = t | zimm; x[rd] = t
      CSRRCI  ->  List(Y,CSR_T,  CSR_RC,   N, RS_IMM,   RS_X,    Y, IMM_Z  ),
      //TODO: am extra inst
    //   HALT    ->  List(Y,FUX_T,        N, RS_X,         RS_X,        N, IMM_X    ),
    //   PUTCH   ->  List(Y,FUX_T,        N, RS_X,         RS_X,        N, IMM_X    )
    )
  )
