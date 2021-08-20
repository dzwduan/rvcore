package core

import chisel3.iotesters
import chisel3.iotesters.Driver

object TestMain extends App{
  iotesters.Driver.execute(args,() => new rvcore) {
    c => new rvcoreTester(c)
  }
}
