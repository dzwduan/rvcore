package core

import Chisel.{fromIntToWidth, fromtIntToLiteral}
import chisel3.iotesters.PeekPokeTester
import chisel3.tester.testableData

import java.io.FileInputStream
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class RVTester(core:RVcore,imgPath:String)  extends PeekPokeTester(core) with pc_init {

  val memSize = 128*1024*1024
  val mem = {
    if(imgPath == ""){
      Array.fill(init_val/4)(0) ++ Array(
        0x07b08093,   // addi x1,x1,123
        0xf8508093,   // addi x1,x1,-123
        0x0000806b,   // trap x1
        0, 0, 0, 0
      )
    }
    else{
      val fc = new FileInputStream("bin").getChannel()
      println(s"bin size = ${fc.size()}")

      val mem = Array.fill(memSize/4)(0)
      fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size()).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(mem,init_val/4,fc.size()/4)

      mem
    }

  }


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
    case 0 => println(f"\33[1;32mHIT GOOD TRAP\33[0m at pc = $pc")
    case 1 => println(f"\33[1;31mHIT BAD TRAP\33[0m at pc = $pc")
    case 2 => println(f"\33[1;31mINVALID OPCODE\33[0m at pc = $pc, instr = $inst")
  }

  expect(core.io.trap,0)
}
