package rvcore


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
      val len = src.getWidth
      if(len == width){
        src
      }
      else Cat(Fill(width-len,msb(src).asUInt()),src)
    }
  }

  object zext{
    def apply(width:Int,src:UInt):UInt ={
      val len = src.getWidth
      if(len == width){
        src
      }
      Cat(Fill(width-len,0.U(1.W)),src)
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


    val imm = MuxLookup(inst_type, sext(32,Iimm) ,
      Seq(
        InstrI -> sext(32,Iimm),
        InstrS -> sext(32,Simm),
        InstrB -> sext(32,Bimm),
        InstrU -> sext(32,Uimm),
        InstrJ -> sext(32,Jimm))
    ).asUInt

    io.out.pc := io.in.pc
    io.out.data.src1 := DontCare
    io.out.data.src2 := imm
    io.out.data.dest := DontCare

    io.out.ctrl.rfWen := isrfWen(inst_type) //only u i r j type can write
    io.out.ctrl.rfDest:= rd
    io.out.ctrl.fuType := fu_type
    io.out.ctrl.fuOpType := fu_op_type
    io.out.ctrl.isTrap := Cat(inst_type===InstrN,inst===TRAP)
    io.out.ctrl.rfSrc1 := rs1
    io.out.ctrl.rfSrc2 := rs2

    val t1::t2::Nil = ListLookup(inst_type,List(Src1Reg, Src2Imm),SrcTypeTable)
    io.out.ctrl.src1Type := t1
    io.out.ctrl.src2Type := t2
    printf("type 1" + t1)
    printf("type 2" + t2)

  }


