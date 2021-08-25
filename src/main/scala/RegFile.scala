package hohai

import chisel3._

class rs1IO extends Bundle{
  val addr = Input(UInt(5.W))
  val data = Output(UInt(64.W))
}

class rs2IO extends Bundle{
  val addr = Input(UInt(5.W))
  val data = Output(UInt(64.W))
}

class rdIO extends Bundle{
  val addr = Input(UInt(5.W))
  val data = Input(UInt(64.W))
  val wen  = Input(Bool())
}

class REGIO extends Bundle{
  val rs1 = new rs1IO 
  val rs2 = new rs2IO 
  val rd  = new rdIO
}

class RegFile extends Module{
  val io = IO(new REGIO)

  val rf = Mem(32,UInt(64.W))

  when(io.rd.wen && io.rd.addr =/=0) {
    rf(io.rd.addr) := io.rd.data
  }

  io.rs1.data := Mux(io.rs1.addr.orR, rf(io.rs1.addr), 0.U)
  io.rs2.data := Mux(io.rs2.addr.orR, rf(io.rs2.addr), 0.U)
}







