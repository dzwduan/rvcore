package rvcore


import chisel3._
import chisel3.util.MuxLookup
import global_config._

class LSU {
  def access(isLsu: Bool, src1: UInt, src2: UInt, func: UInt, wdata: UInt): MemIO = {
    val dmem = Wire(new MemIO)
    dmem.out.bits.wen := func(3) & isLsu
    dmem.out.bits.addr := src1 + src2
    dmem.out.bits.wdata := wdata
    dmem.out.bits.size := func(1, 0)
    dmem.out.valid := isLsu

    dmem
  }

  def rdataExt(rdata: UInt, func: UInt): UInt = {
    rdata := MuxLookup(func,0.U,Array(
      LsuLb ->  sext(xlen,rdata(7,0)),
      LsuLh ->  sext(xlen,rdata(15,0)),
      LsuLw ->  sext(xlen,rdata(31,0)),
      LsuLd ->  sext(xlen,rdata(63,0)),
      LsuLbu -> zext(xlen,rdata(7,0)),
      LsuLhu -> zext(xlen,rdata(15,0)),
      LsuLwu -> zext(xlen,rdata(31,0))
    ))
    rdata
  }
}
