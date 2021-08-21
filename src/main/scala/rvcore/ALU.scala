package rvcore

import chisel3._
import _root_.rvcore.global_config._
import chisel3.util.MuxLookup

object ALU {
  def apply(src1:UInt,src2:UInt,func:UInt):UInt = {
    val shamt = src2(4, 0)
    val funcList = List(
      AluAdd  -> (src1  +  src2),
      AluSll  -> ((src1  << shamt)(31, 0)),
      AluSlt  -> ((src1.asSInt < src2.asSInt).asUInt),
      AluSltu -> ((src1 < src2).asUInt),
      AluXor  -> (src1  ^  src2),
      AluSlr  -> (src1  >> shamt),
      AluOr   -> (src1  |  src2),
      AluAnd  -> (src1  &  src2),
      AluSub  -> (src1  -  src2),
      AluSar  -> ((src1.asSInt >> shamt).asUInt)
    )
   MuxLookup(func,0.U,funcList).asUInt()
  }
}


