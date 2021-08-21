package top
import chisel3._
import rvcore._

object TopMain extends App {
  Driver.execute(args,()=>new RVcore)
}
