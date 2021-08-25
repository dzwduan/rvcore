import chisel3._ 


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
