package bf_interpreters.bytecode

import bf_interpreters.BrainfuckInterpreter

class OptimizedBytecodeInterpreter : BrainfuckInterpreter {
    sealed class Bytecode {
        companion object {
            const val NOP : Short = 0

            const val MOVE : Short = 1
            const val ADD : Short = 2
            const val PRINT : Short = 3
            const val READ : Short = 4
            const val LOOP_OPEN : Short = 5
            const val LOOP_CLOSE : Short = 6
        }
    }

    data class OpenLoop(val lineNumber : Int ,val linePosition : Int, val codeIndex : Int)
    override fun compile(program: String): Any {
        val code : MutableList<Short> = mutableListOf<Short>();

        val lines = program.split("\n")
        val openLoops = ArrayDeque<OpenLoop>();

        var runLength : Short = 0;
        var runLengthInstruction : Short = Bytecode.NOP

        for (li in 0..<lines.size) {
            for (col in 0..<lines[li].length) {
                val c = lines[li][col]
                // Add the instruction to bytecode at the end of sequence
                when(runLengthInstruction) {
                    Bytecode.MOVE -> { if( !(c == '<' || c == '>') ) {
                        code.add(Bytecode.MOVE)
                        code.add(runLength)
                        runLength = 0
                        runLengthInstruction = Bytecode.NOP
                    }}
                    Bytecode.ADD -> { if( !(c == '+' || c == '-' )) {
                        code.add(Bytecode.ADD)
                        code.add(runLength)
                        runLength = 0
                        runLengthInstruction = Bytecode.NOP
                    }}
                }

                // Process instruction
                when(c) {
                    '>' -> { runLengthInstruction = Bytecode.MOVE; runLength++; }
                    '<' -> { runLengthInstruction = Bytecode.MOVE; runLength--; }
                    '+' -> { runLengthInstruction = Bytecode.ADD; runLength++; }
                    '-' -> { runLengthInstruction = Bytecode.ADD; runLength--; }
                    '.' -> { code.add(Bytecode.PRINT);}
                    ',' -> { code.add(Bytecode.READ);}
                    '[' -> {
                        val openLoop = OpenLoop(li, col, code.size)
                        openLoops.add(openLoop)
                        code.add(Bytecode.LOOP_OPEN)
                        // Placeholder for the forward jump relative address
                        code.add(Bytecode.NOP)
                    }
                    ']' -> {
                        if(openLoops.isEmpty()) throw Error("Unopened cycle - line ${li+1} character ${col+1}")
                        val opening = openLoops.removeLast()

                        code.add(Bytecode.LOOP_CLOSE)
                        val jumpBackTarget = opening.codeIndex - 1
                        code.add(jumpBackTarget.toShort())

                        // Forward jump target is past the closing bracket
                        // subtract 1 because it gets added back when the program counter increments by 1 after instruction execution
                        val jumpForwardTarget = code.size - 1
                        code[opening.codeIndex + 1] = jumpForwardTarget.toShort()
                    }
                }
            }
        }

        if(openLoops.isNotEmpty()) {throw Error("Unclosed cycle - line ${lines.size} character ${lines.last().length}")
        }
        // Move and Add instructions have no effect at the very end of code,
        // remaining run of MOVE or ADD is discarded

        return code.toShortArray();
    }

    override fun run(executable: Any, tapeSize: Int) {
        val program = executable as ShortArray
        val cells = ByteArray(tapeSize)
        var pc = 0
        var cp = 0 // cell pointer
        val zero : Byte = 0

        while (pc < program.size) {
            when (program[pc]) {
                Bytecode.MOVE -> {
                    pc += 1
                    cp += program[pc]
                    if(cp < 0) {
                        throw Error("Memory underrun")
                    }
                    if(cp >= tapeSize) {
                        throw Error("Memory overrun")
                    }
                }
                Bytecode.ADD -> {
                    pc += 1
                    cells[cp] = (cells[cp] + program[pc]).toByte()
                }
                Bytecode.PRINT -> {
                    print(cells[cp].toUByte().toInt().toChar())
                }
                Bytecode.LOOP_OPEN -> {
                    pc += 1 // LOOP_OPEN code has 1 parameter
                    if(cells[cp] == zero) {
                        val jumpTarget = program[pc]
                        pc = jumpTarget.toInt()
                    }

                }
                Bytecode.LOOP_CLOSE -> {
                    pc += 1
                    if(cells[cp] != zero) {
                        val jumpTarget = program[pc]
                        pc = jumpTarget.toInt()
                    }

                }
                Bytecode.READ -> {
                    val c = System.`in`.read()
                    if(c == -1) throw Exception("End of input")
                    cells[cp] = c.toByte()
                }

            }
            pc += 1
        }
    }
}