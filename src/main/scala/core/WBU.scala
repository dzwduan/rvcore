package core

import chisel3._

class WBUIO extends Bundle{
  val in = Flipped(new PcCtrlDataIO)
  val wb = new WriteBackIO
}

class WBU extends Module{
  val io = IO(new WBUIO)

  io.wb.rfWdata := io.in.data.dest
  io.wb.rfWen   := io.in.ctrl.rfWen
  io.wb.rfDest  := io.in.ctrl.rfDest
}
