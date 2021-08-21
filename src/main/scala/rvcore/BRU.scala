package rvcore

import chisel3._
import chisel3.util.MuxLookup
import global_config._

//jal  x(rd) = pc + 4
//jalr t = pc + 4; pc = (x(rs1) + sext(offset)) & ~(1);x[rd]=t

class  BRU {
  def apply(src1:UInt,src2:UInt,func:UInt):(Bool,UInt) = {
    val funcList = List(
      BruJal  -> (src1 + src2),
      BruJalr -> (src1 + src2)
    )
    val target = MuxLookup(func,0.U,funcList).asUInt()
    val isTaken = func(3)
    (isTaken,target)
  }
}
