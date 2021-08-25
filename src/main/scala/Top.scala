package hohai

import difftest._
import chisel3._

class COREIO extends Bundle{
  //TODO
  val pc = Output(UInt(64.W))
  val inst = Input(UInt(32.W))
}

class Top extends Module {

  val core = Module(new Core())
	io <> core.io
}
