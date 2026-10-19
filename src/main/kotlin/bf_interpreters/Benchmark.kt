package bf_interpreters

import bf_interpreters.bytecode.BytecodeInterpreter
import bf_interpreters.bytecode.OptimizedBytecodeInterpreter
import bf_interpreters.`object`.ObjectInterpreter
import bf_interpreters.`object`.OptimizedObjectInterpreter
import bf_interpreters.`object`.VirtualMethodInterpreter
import java.io.File
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

fun benchmark(interpreter : BrainfuckInterpreter, name: String, bfSource : String) {
    System.err.print("BENCHMARK: $name; ")
    val executable = interpreter.compile(bfSource)
    val REPEATS = 10
    val measurements = Array<Long>(10){0}
    for (i in 1..REPEATS) {
        val timeElapsed = measureTimeMillis {
            // some code block
            interpreter.run(executable)
        }
        measurements[i-1] += timeElapsed
    }
    val mean = measurements.sum() / REPEATS
    var variance = 0.0
    for (i in 0..<REPEATS) {
        variance += (measurements[i] - mean).toDouble().pow(2.0)
    }
    variance /= REPEATS

    val std : Int = sqrt(variance).roundToInt() // Standard Deviation
    System.err.println("${mean}ms ± ${std}")

}

fun main(args: Array<String>) {
//    val interpreter : BrainfuckInterpreter = BytecodeInterpreter()
//    val executable = interpreter.compile("++++++++++[>+++++++>++++++++++>+++>+<<<<\n" +
//            "-]>++.>+.+++++++..+++.>++.<<++++++++++++\n" +
//            "+++.>.+++.------.--------.>+.>.")
//    interpreter.run(executable = executable)
//    System.exit(0)

    val bfSourceCode = File(args[0]).readText()
    benchmark(VirtualMethodInterpreter(), "VirtualMethodInterpreter", bfSourceCode)
    benchmark(ObjectInterpreter(), "ObjectInterpreter", bfSourceCode)
    benchmark(BytecodeInterpreter(), "BytecodeInterpreter", bfSourceCode)
    benchmark(OptimizedObjectInterpreter(), "OptimizedObjectInterpreter", bfSourceCode)
    benchmark(OptimizedBytecodeInterpreter(), "OptimizedBytecodeInterpreter", bfSourceCode)


}

class Benchmark {
}