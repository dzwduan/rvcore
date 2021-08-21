package rvcore

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
      val fc = new FileInputStream(imgPath).getChannel()
      println(s"bin size = 0x${fc.size()}%08x")

      val mem = Array.fill(memSize/4)(0)
      fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size()).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(mem,init_val/4,fc.size()/4)
      mem
    }
  }

  var pc = 0
  var trap = 0
  var inst = 0

  do {
    pc = peek(core.io.imem.out.bits.addr).toInt
    assert((pc & 0x3) == 0)
    inst = mem(pc >> 2) & 0xffffffff
    poke(core.io.imem.in.rdata, inst)

    val valid = peek(core.io.dmem.out.valid)
    if(valid == 1){
      val dmemAddr = peek(core.io.dmem.out.bits.addr).toInt
      val size = peek(core.io.dmem.out.bits.size).toInt
      val (addrMask,dataMask) = size match {
        case 0 => (0,0xff)
        case 1 => (1,0xffff)
        case 2 => (2,0xffffffff)
      }
      assert((dmemAddr&addrMask)==0) //addr must jmp by masksize
      val addr = dmemAddr>>2
      val offset = dmemAddr & 0x3
      val data = mem(addr)
      val rdataAlign = data >> (offset*8)
      poke(core.io.dmem.in.rdata,rdataAlign)

      val wen = peek(core.io.dmem.out.bits.wen)
      if(wen==1){
        val wdata = peek(core.io.dmem.out.bits.wdata).toInt
        val wdataAlign = wdata << (offset * 8)
        val dataMaskAlign = dataMask << (offset * 8)
        val newData = (data & ~dataMaskAlign) | (wdataAlign & dataMaskAlign)
        mem(addr) = newData

        println(f"wdata = 0x$wdata%08x, realWdata = 0x$newData%08x, offset = $offset")
      }
      else{
        println(f"rdataAlign = 0x$rdataAlign%08x")
      }
    }
    step(1)

    trap = peek(core.io.trap).toInt
  }while(trap == 3)


  trap match {
    case 0 => println(f"\33[1;32mHIT GOOD TRAP\33[0m at  pc = 0x$pc%08x")
    case 1 => println(f"\33[1;31mHIT BAD TRAP\33[0m at   pc = 0x$pc%08x")
    case 2 => println(f"\33[1;31mINVALID OPCODE\33[0m at pc = 0x$pc%08x, instr = 0x$inst%08x")
  }

  expect(core.io.trap,0)
}
