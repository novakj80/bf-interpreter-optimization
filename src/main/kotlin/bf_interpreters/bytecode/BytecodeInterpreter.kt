package bf_interpreters.bytecode

import bf_interpreters.BrainfuckInterpreter

class BytecodeInterpreter : BrainfuckInterpreter {
    sealed class Bytecode {
        companion object {
            const val NOP : Int = 0

            const val MOVE_LEFT : Int = 1
            const val MOVE_RIGHT : Int = 2
            const val INCREMENT : Int = 3
            const val DECREMENT : Int = 4
            const val PRINT : Int = 5
            const val READ : Int = 6
            const val LOOP_OPEN : Int = 7
            const val LOOP_CLOSE : Int = 8
        }
    }

    data class OpenLoop(val lineNumber : Int ,val linePosition : Int, val codeIndex : Int)
    override fun compile(program: String): Any {
        val code : MutableList<Int> = mutableListOf<Int>();

        val lines = program.split("\n")
        val openLoops = ArrayDeque<OpenLoop>();

        for (li in 0..<lines.size) {
            for (c in 0..<lines[li].length) {
                when(lines[li][c]) {
                    '>' -> { code.add(Bytecode.MOVE_RIGHT); }
                    '<' -> { code.add(Bytecode.MOVE_LEFT); }
                    '+' -> {code.add(Bytecode.INCREMENT)}
                    '-' -> {code.add(Bytecode.DECREMENT)}
                    '.' -> {code.add(Bytecode.PRINT)}
                    ',' -> {code.add(Bytecode.READ)}
                    '[' -> {
                        val openLoop = OpenLoop(li, c, code.size)
                        openLoops.add(openLoop)
                        code.add(Bytecode.LOOP_OPEN)
                        // Placeholder for the forward jump relative address
                        code.add(Bytecode.NOP)
                    }
                    ']' -> {
                        if(openLoops.isEmpty()) throw Error("Unopened cycle - line ${li+1} character ${c+1}")
                        val opening = openLoops.removeLast()

                        code.add(Bytecode.LOOP_CLOSE)
                        val jumpBackTarget = opening.codeIndex - 1
                        code.add(jumpBackTarget)

                        // Forward jump target is past the closing bracket
                        // subtract 1 because it gets added back when the program counter increments by 1 after instruction execution
                        val jumpForwardTarget = code.size - 1
                        code[opening.codeIndex + 1] = jumpForwardTarget
                    }
                }
            }
        }

        if(openLoops.isNotEmpty()) {throw Error("Unclosed cycle - line ${lines.size} character ${lines.last().length}")
        }

        return code.toIntArray()
    }

    override fun run(executable: Any, tapeSize: Int) {
        val program = executable as IntArray
        val cells = Array<UByte>(tapeSize) {0u}
        var pc = 0
        var cp = 0 // cell pointer

        while (pc < program.size) {
            when (program[pc]) {
                Bytecode.MOVE_LEFT -> {
                    cp--
                    if(cp < 0) {
                        throw Exception("Move must be between 0 and $tapeSize")
                    }
                }
                Bytecode.MOVE_RIGHT -> {
                    cp++
                    if(cp >= tapeSize) {
                        throw Exception("Move must be between 0 and $tapeSize")
                    }
                }
                Bytecode.INCREMENT -> {
                    cells[cp]++
                }
                Bytecode.DECREMENT -> {
                    cells[cp]--
                }
                Bytecode.PRINT -> {
                    print(cells[cp].toInt().toChar())
                }
                Bytecode.READ -> {
                    val c = System.`in`.read()
                    if(c == -1) throw Exception("End of input")
                    cells[cp] = c.toUByte()
                }
                Bytecode.LOOP_OPEN -> {
                    pc += 1 // LOOP_OPEN code has 1 parameter
                    if(cells[cp] == 0.toUByte()) {
                        val jumpTarget = program[pc]
                        pc = jumpTarget
                    }

                }
                Bytecode.LOOP_CLOSE -> {
                    pc += 1
                    if(cells[cp] != 0.toUByte()) {
                        val jumpTarget = program[pc]
                        pc = jumpTarget
                    }

                }

            }
            pc += 1
        }
    }
}