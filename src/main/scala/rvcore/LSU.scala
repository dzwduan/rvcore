package rvcore


import chisel3._
import chisel3.util.MuxLookup
import global_config._

object LSU {
  def apply(src1:UInt,src2:UInt,func:UInt):(Bool,UInt) = {
    val funcList = List(
      LsuSw -> (src1+src2)
    )
    val addr = MuxLookup(func,0.U,funcList).asUInt()
    val wen = func(3)
    (wen,addr)
  }
}
