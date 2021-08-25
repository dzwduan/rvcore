package hohai

import chisel3._


class Core extends Module {
  val io = IO(new COREIO)

  val pc = RegInit(0.U(64.W))
  io.pc := pc

  val idu = Module(new IDU)
  idu.io.inst := io.inst
  idu.io.pc   := pc

  val cd = idu.io.ctrl_data

  //get related reg data and reg wen
  val rf = Module(new RegFile)
  rf.io.rs1.addr := cd.rs1_addr
  rf.io.rs2.addr := cd.rs2_addr
  rf.io.rd.addr  := cd.rd_addr
  rf.io.rd.wen   := cd.rd_en
  
  val exu = Module(new EXU)
  exu.io.ctrl_data := cd
  exu.io.in1_data := rf.io.rs1_data
  exu.io.in2_data := rf.io.rs2_data
  rf.io.rd_data   := exu.io.out_data                  
  //update pc
  pc := exu.io.nextPC
}
