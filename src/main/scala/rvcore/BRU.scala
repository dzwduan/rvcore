package rvcore

import chisel3._
import chisel3.util.MuxLookup
import global_config._

//jal  x(rd) = pc + 4
//jalr t = pc + 4; pc = (x(rs1) + sext(offset)) & ~(1);x[rd]=t

class  BRU {
  def access(isBru:Bool,pc:UInt,offset:UInt,src1:UInt,src2:UInt,func:UInt):BranchIO = {
    val br = Wire(new BranchIO)

    br.target := Mux(func===BruJalr,src1+offset,pc+offset)
    br.isTaken := isBru && MuxLookup(func,false.B,Array(
      BruBeq -> (src1 === src2),
      BruBne -> (src1 =/= src2),
      BruBlt -> (src1.asSInt < src2.asSInt),
      BruBge -> (src1.asSInt >= src2.asSInt),
      BruBltu -> (src1.asUInt < src2.asUInt),
      BruBgeu -> (src1.asUInt >= src2.asUInt),
      BruJal  -> true.B,
      BruJalr -> true.B
    ))
    br
  }
}
