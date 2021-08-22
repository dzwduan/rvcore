package core

import chisel3._
import chiseltest._
import chiseltest.experimental.TestOptionBuilder._
import org.scalatest.FreeSpec
import rvcore._
import treadle.WriteVcdAnnotation

import java.io.FileInputStream
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import Const._
import chisel3.iotesters.PeekPokeTester._


object Const {
  def ULONG_MAX : BigInt = BigInt(Long.MaxValue) * 2 + 1

}

class TopTester extends FreeSpec with ChiselScalatestTester with pc_init{
  def SERIAL_MMIO_ADDR = BigInt(0xa10003f8L)
//  def show_regfile(top: RVcore) : Unit = {
//    for (i <- 0 to 24) {
//      top.io.debug.addr.poke(i.U)
//      val reg = top.io.debug.data.peek().litValue()
//      println(f"rf[$i%2d] : $reg%016x")
//    }
//  }
  def mem_write(mem: Array[Int], core_dmem_addr: BigInt, wdata: BigInt, rdata: BigInt, data_type: Int) : Unit = {
    val (_, data_mask) = data_type match {
      case 0 => (0, BigInt(0xff))
      case 1 => (0x1, BigInt(0xffff))
      case 2 => (0x3, BigInt(0xffffffffL))
      case 3 => (0x7, ULONG_MAX)
    }

    val is_uart = core_dmem_addr == SERIAL_MMIO_ADDR
    if (is_uart) {
      print((wdata&0xff).toChar)
    }
    else {
      val dmem_addr = addrMap(core_dmem_addr)
      val addr = ((dmem_addr >> 3) << 1).toInt
      val offset = (dmem_addr & 0x7).toInt  // [2:0], for ld and sd
      val wdata_align = wdata << (offset * 8)
      val data_mask_align = data_mask << (offset * 8)
      // read before write
      val newData = (rdata & ~data_mask_align) | (wdata_align & data_mask_align)
      mem(addr) = (newData & 0xffffffffL).toInt
      mem(addr + 1) = ((newData >> 32) & 0xffffffffL).toInt
    }

  }
  def mem_read(mem: Array[Int], core_dmem_addr: BigInt, data_type: Int) : (BigInt, BigInt) = {
    val (addr_mask, _) = data_type match {
      case 0 => (0, BigInt(0xff))
      case 1 => (0x1, BigInt(0xffff))
      case 2 => (0x3, BigInt(0xffffffffL))
      case 3 => (0x7, ULONG_MAX)
    }
    val is_uart = core_dmem_addr == SERIAL_MMIO_ADDR
    if (!is_uart) {
      assert((core_dmem_addr & addr_mask) == 0, f"dmem_addr=0x$core_dmem_addr%08X, addr_mask=$addr_mask")
      val dmem_addr = addrMap(core_dmem_addr)
      val addr = ((dmem_addr >> 3) << 1).toInt
      val offset = (dmem_addr & 0x7).toInt  // [2:0], for ld and sd
      val data1 = mem(addr)
      val data2 = mem(addr + 1)
      //    println(f"core_dmem_addr: $core_dmem_addr%016X, dmem_addr: $dmem_addr%16X, addr: $addr%08X, offset: $offset, data1: $data1, data2: $data2")
      val rdata = (BigInt(data2.toLong & 0xffffffffL) << 32) | BigInt(data1.toLong & 0xffffffffL)
      val rdata_align = rdata >> (offset * 8)
      (rdata_align, rdata)
    }
    else {
      Tuple2(BigInt(0), BigInt(0))
    }

  }
  "Top test" in {
    test(new RVcore).withAnnotations(Seq(WriteVcdAnnotation)) {
      top =>

        //        val imgPath = "z:/home/huxuan/repo/am-kernels/tests/cpu-tests/single_tests/asm/add.bin"
        val imgPath = "/home/dzw/rvcore/inst/build/sum-riscv64-mycpu.bin"
        var numCheck = 0
        val checkAddr = 0x80000010
        val memSize = 256*1024*1024
        val mem = {
          if (imgPath=="") {
            val mem = Array.fill((addrMap(pc_init) / 4).toInt)(0) ++ Array(
              0xf8508093,   // addi x1,x1,-123
              0x07b08093,   // addi x1,x1,123
              //        0x0000806b,   // trap x1
              2, 1, 0
            )
            mem
          } else {
            val fc = new FileInputStream(imgPath).getChannel
            println(f"bin size = 0x${fc.size()}%08x")
            val mem = Array.fill(memSize / 4)(0)
            fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).order(ByteOrder.LITTLE_ENDIAN)
              .asIntBuffer().get(mem, (addrMap(pc_init) / 4).toInt, fc.size().toInt / 4)
            //            for (i <- (addrMap(pc_init) / 4).toInt until (addrMap(pc_init) / 4).toInt + fc.size().toInt / 4) {
            //              val data = mem(i)
            //              println(f"${(i * 4) + pc_shift}%04x: $data%08x")
            //            }
            mem
          }
        }

        var pc = 0
        var trap = 0
        var instr = 0L
        do {
          pc = top.io.imem.out.bits.addr.peek().litValue().toInt
          if (pc == checkAddr)
            numCheck += 1
          //          print(f"pc: $pc%08x  ")
          instr = mem(addrMap(pc).toInt >> 2) & 0xffffffffL
          //          print(f"inst: $instr%08x  ")
          top.io.imem.in.rdata.poke(instr.U(32.W))
          val dmem_valid:Boolean = top.io.dmem.out.valid.peek().litToBoolean

          // build a virtual dmem
          if(dmem_valid) {
            val dmem_addr = top.io.dmem.out.bits.addr.peek().litValue() & ULONG_MAX
            val data_type = top.io.dmem.out.bits.size.peek().litValue().toInt
            val (rdata_align, rdata) = mem_read(mem, dmem_addr, data_type)
            top.io.dmem.in.rdata.poke(rdata_align.U(64.W))
            val wena = top.io.dmem.out.bits.wen.peek().litToBoolean
            var wdata = BigInt(0)
            if(wena) {
              wdata = top.io.dmem.out.bits.wdata.peek().litValue()
              mem_write(mem, dmem_addr, wdata, rdata, data_type)
            }
            //            print("dmem_valid ")
            //            if (wena) {
            //              print(f"write at 0x$dmem_addr%08X with 0x$wdata%016X use type$data_type")
            //            } else {
            //              print(f"read at 0x$dmem_addr%08X with 0x$rdata%016X use type$data_type")
            //            }
          }
          top.clock.step()
          //          println()
          //          show_regfile(top)
          trap = top.io.trap.peek().litValue().toInt

        } while (trap==3 && pc != top.io.imem.out.bits.addr.peek().litValue().toInt)
        println(f"last_inst: $instr%08x at pc: $pc%08x")
        println(f"num of check: $numCheck")
        trap match {
          case 0 => println(f"\33[1;32mHIT GOOD TRAP\33[0m at pc = 0x$pc%08x")
          case 1 => println(f"\33[1;31mHIT BAD TRAP\33[0m at pc = 0x$pc%08x")
          case 2 => println(f"\33[1;31mINVALID OPCODE\33[0m at pc = 0x$pc%08x, instr = 0x$instr%08x")
        }
        top.io.trap.expect(0.asUInt())
        fork {

        }.join()

    }

  }
}

