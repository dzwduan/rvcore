package core


import chisel3._
import chisel3.util._
import global_config._

import scala.collection.immutable.Nil

//input pc inst ;  output ctrl signals, and get imm

//helper functions
object msb{
  def apply(data:UInt):Bool ={
    data(data.getWidth-1).asBool()
  }
}

object sext{
  def apply(width:Int,src:UInt):UInt ={
    Cat(Fill(width-src.getWidth,msb(src).asUInt()),src)
  }
}

object zext{
  def apply(width:Int,src:UInt):UInt ={
    Cat(Fill(width-src.getWidth,0.U(1.W)),src)
  }
}


class IDUIO extends Bundle{
  val in = Flipped(new PcInstrIO)
  val out = new PcCtrlDataIO
}


//get signals from decodeTable and inst

class IDU extends Module{
  val io = IO(new IDUIO)

  val inst = io.in.instr
  val pc   = io.in.pc

  val func3 = inst(14,12)
  val func7 = inst(31,25)
  val rs1   = inst(19,15)
  val rs2   = inst(24,20)
  val rd    = inst(11,7)

  val inst_type::fu_type::fu_op_type::Nil = ListLookup(inst,DecodeDefault,DecodeTable)

  val Iimm = inst(31, 20)
  val Simm = Cat(inst(31, 25), inst(11,7))
  val Bimm = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W))
  val Uimm = Cat(inst(31, 12), 0.U(12.W))
  val Jimm = Cat(inst(31), inst(19, 12), inst(20), inst(30, 25), inst(24, 21), 0.U(1.W))
  val Zimm = inst(19, 15)

  printf("get in ImmGen\n")

  val imm = MuxLookup(inst_type, sext(64,Iimm) ,
    Seq(
      InstrI -> sext(64,Iimm),
      InstrS -> sext(64,Simm),
      InstrB -> sext(64,Bimm),
      InstrU -> sext(64,Uimm),
      InstrJ -> sext(64,Jimm))
  ).asUInt

  
}
