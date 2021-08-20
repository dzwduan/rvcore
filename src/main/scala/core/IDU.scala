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

    printf("get in ImmGen\n")

    val imm = MuxLookup(inst_type, sext(64,Iimm) ,
      Seq(
        InstrI -> sext(64,Iimm),
        InstrS -> sext(64,Simm),
        InstrB -> sext(64,Bimm),
        InstrU -> sext(64,Uimm),
        InstrJ -> sext(64,Jimm))
    ).asUInt

    io.out.pc := io.in.pc
    io.out.data.src1 := DontCare
    io.out.data.src2 := imm
    io.out.data.dest := DontCare

    io.out.ctrl.rfWen := isrfWen(inst_type) //only u i r j type can write
    io.out.ctrl.rfDest:= DontCare
    io.out.ctrl.fuType := fu_type
    io.out.ctrl.fuOpType := fu_op_type
    io.out.ctrl.isTrap := Cat(inst_type===InstrN,inst===TRAP)
    io.out.ctrl.rfSrc1 := rs1
    io.out.ctrl.rfSrc2 := rs2

    io.out.ctrl.src1Type := MuxLookup(inst_type,0.U,SrcTypeTable.map(p=>(p._1,p._2._1)))
    io.out.ctrl.src1Type := MuxLookup(inst_type,0.U,SrcTypeTable.map(p=>(p._1,p._2._2)))

  }


