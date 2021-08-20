package core

import Chisel.{fromIntToWidth, fromtIntToLiteral}
import chisel3.iotesters.PeekPokeTester
import chisel3.tester.testableData

class RVTester(core:RVcore)  extends PeekPokeTester(core){
  val mem = List(
    0x07b08093,   // addi x1,x1,123
    0xf8508093,   // addi x1,x1,-123
    0x0000806b,   // trap x1
    0, 0, 0, 0
  )

  var pc = 0x80000000L
  var trap = 0
  var inst = 0

  do {
    pc = core.io.imem.out.bits.addr.peek().litValue().toInt
    println(f"pc: $pc%08x")
    assert((pc & 0x3) == 0)
    inst = mem(pc >> 2) & 0xffffffffL
    println(f"inst: $inst%08x")
    core.io.imem.in.rdata.poke(inst.U(32.W))
    step(1)
    trap = (core.io.trap).toInt
  }while(trap == 3)


  trap match {
    case 0 => println(s"\33[1;32mHIT GOOD TRAP\33[0m at pc = $pc")
    case 1 => println(s"\33[1;31mHIT BAD TRAP\33[0m at pc = $pc")
    case 2 => println(s"\33[1;31mINVALID OPCODE\33[0m at pc = $pc, instr = $inst")
  }
}
