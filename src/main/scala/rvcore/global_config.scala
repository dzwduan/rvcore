package rvcore

import Chisel.{BitPat, ListLookup}
import chisel3._
import chisel3.util.log2Up

object global_config {

  /* instruction type */
  private val InstrTypeNum = 7
  val InstrN = "b000".U
  val InstrI = "b100".U
  val InstrR = "b101".U
  val InstrS = "b010".U
  val InstrB = "b001".U
  val InstrU = "b110".U
  val InstrJ = "b111".U
  val InstrTypeWidth = log2Up(InstrTypeNum).W

  def isrfWen(instrType : UInt): Bool = instrType(2)

  /* src1 type */
  private val Src1TypeNum = 2
  val Src1Reg = "b0".U
  val Src1Pc  = "b1".U
  val Src1TypeWidth = log2Up(Src1TypeNum).W

  /* src2 type */
  private val Src2TypeNum = 2
  val Src2Imm = "b0".U
  val Src2Reg = "b1".U
  val Src2TypeWidth = log2Up(Src2TypeNum).W



  val SrcTypeTable = Array(
    BitPat(InstrI) ->  List(Src1Reg, Src2Imm),
    BitPat(InstrR) ->  List(Src1Reg, Src2Reg),
    BitPat(InstrS) ->  List(Src1Reg, Src2Imm),
    BitPat(InstrB) ->  List(Src1Reg, Src2Reg),
    BitPat(InstrU) ->  List(Src1Pc , Src2Imm),
    BitPat(InstrJ) ->  List(Src1Pc , Src2Imm),
    BitPat(InstrN) ->  List(Src1Pc , Src2Imm)
  )

  /* function unit type */
  private val FuTypeNum = 4
  val FuAlu = "b00".U
  val FuBru = "b01".U
  val FuLsu = "b10".U
  val FuMdu = "b11".U
  val FuTypeWidth = log2Up(FuTypeNum).W

  /* ALU operation type */
  private val FuOpTypeAluNum  = 10
  val AluAdd  = "b0000".U
  val AluSll  = "b0001".U
  val AluSlt  = "b0010".U
  val AluSltu = "b0011".U
  val AluXor  = "b0100".U
  val AluSlr  = "b0101".U
  val AluOr   = "b0110".U
  val AluAnd  = "b0111".U
  val AluSub  = "b1000".U
  val AluSar  = "b1101".U

  /* BRU operation type */
  private val FuOpTypeBruNum  = 10
  val BruJal  = "b1000".U
  val BruJalr = "b1001".U

  /* LSU operation type */
  private val FuOpTypeLsuNum  = 10
  val LsuSw   = "b1010".U


  /* MDU operation type */
  private val FuOpTypeMduNum  = 0

  private val FuOpTypeMaxNum = List(FuOpTypeAluNum, FuOpTypeBruNum,
    FuOpTypeLsuNum, FuOpTypeMduNum).reduce(math.max)
  val FuOpTypeWidth = log2Up(FuOpTypeMaxNum).W


  /* instruction pattern */
  val ADDI    = BitPat("b????????????_?????_000_?????_0010011")

  val AUIPC   = BitPat("b????????????????????_?????_0010111")

  val JAL     = BitPat("b????????????????????_?????_1101111")
  val JALR    = BitPat("b????????????_?????_000_?????_1100111")

  val SW      = BitPat("b???????_?????_?????_010_?????_0100011")

  val TRAP    = BitPat("b????????????_?????_000_?????_1101011")


  /* decode table */
  val DecodeDefault = List( InstrN, FuAlu, AluAdd)
  val DecodeTable = Array(
    /*                      Instr |  FU  | FU OP |
     *                      Type  | Type |  Type | */
               ADDI -> List(InstrI, FuAlu, AluAdd),
               AUIPC-> List(InstrU, FuAlu, AluAdd),
               JAL  -> List(InstrJ, FuBru, BruJal),
               JALR -> List(InstrI, FuBru, BruJalr),
               SW   -> List(InstrS, FuLsu, LsuSw),
               TRAP -> List(InstrI, FuAlu, AluAdd)
  )
}
