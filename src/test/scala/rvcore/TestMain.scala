package rvcore

import chisel3.iotesters
import chisel3.iotesters.Driver


object TestMain extends App{
  var imgPath = "/home/dzw/rvcore/inst/build/dummy-riscv64-mycpu.bin"
  var newArgs:Array[String] = Array()
  args.sliding(2,2).toList.collect{
    case Array("--img",argImg:String) => imgPath = argImg
    case Array(a:String,b:String) => newArgs:+a :+b
  }
  iotesters.Driver.execute(newArgs,() => new RVcore) {
    c => new RVTester(c,imgPath)
  }
}

