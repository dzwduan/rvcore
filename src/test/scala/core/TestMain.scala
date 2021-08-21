package core

import chisel3.iotesters
import chisel3.iotesters.Driver


object TestMain extends App{
  var imgPath = ""
  var newArgs:Array[String] = Array()
  args.sliding(2,2).toList.collect{
    case Array("--img",argImg:String) => imgPath = argImg
    case Array(a:String,b:String) => newArgs:+a :+b
  }
  iotesters.Driver.execute(newArgs,() => new RVcore) {
    c => new RVTester(c,imgPath)
  }
}

