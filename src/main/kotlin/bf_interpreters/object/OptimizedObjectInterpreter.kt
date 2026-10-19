package bf_interpreters.`object`

import bf_interpreters.bytecode.OptimizedBytecodeInterpreter.Bytecode

class OptimizedObjectInterpreter : bf_interpreters.BrainfuckInterpreter {
    class Opening(val lineNumber : Int, val linePosition : Int, val openInstructionIndex: Int)
    override fun compile(program: String): Any {
        val instructions = mutableListOf<OptimizedInstruction>()

        val lines = program.split("\n")
        val openLoops = ArrayDeque<Opening>();
        var runLength : Int = 0;
        var insideMoveSequence = false
        var insideAddSequence = false

        for (li in 0..<lines.size) {
            for (col in 0..<lines[li].length) {
                val c = lines[li][col]

                if(insideMoveSequence) {
                    // Add instruction when program reached the end of sequence
                    if( !(c == '<' || c == '>') ) {
                        instructions.add(OptimizedInstruction.Move(runLength))
                        runLength = 0
                        insideMoveSequence = false
                    }
                }

                if(insideAddSequence) {
                    if( !(c == '+' || c == '-') ) {
                        instructions.add(OptimizedInstruction.Add(runLength))
                        runLength = 0
                        insideAddSequence = false
                    }
                }

                when(lines[li][col]) {
                    '>' -> { insideMoveSequence = true; runLength++; }
                    '<' -> { insideMoveSequence = true; runLength--; }
                    '+' -> { insideAddSequence = true; runLength++; }
                    '-' -> { insideAddSequence = true; runLength--; }
                    '.' -> { instructions.add(OptimizedInstruction.Print())}
                    ',' -> { instructions.add(OptimizedInstruction.Read()) }
                    '[' -> {
                        val openInstruction = OptimizedInstruction.LoopOpen()
                        val openLoop = Opening(li, col, instructions.size)
                        openLoops.add(openLoop)
                        instructions.add(openInstruction)
                    }
                    ']' -> {
                        if(openLoops.isEmpty()) throw Error("Unopened cycle - line ${li+1} character ${col+1}")
                        val opening = openLoops.removeLast()

                        val openingInstruction : OptimizedInstruction.LoopOpen = instructions[opening.openInstructionIndex] as OptimizedInstruction.LoopOpen
                        openingInstruction.jumpTarget = instructions.size

                        val closingInstruction = OptimizedInstruction.LoopClose()
                        closingInstruction.jumpTarget = opening.openInstructionIndex - 1
                        instructions.add(closingInstruction)
                    }
                }
            }
        }

        if(openLoops.isNotEmpty()) {throw Error("Unclosed cycle - line ${lines.size} character ${lines.last().length}")
        }
        // Move and Add instructions have no effect at the very end of code,
        // remaining run of Move or Add is discarded

        return instructions.toTypedArray()
    }


    override fun run(executable: Any, tapeSize: Int) {
        val instructions = executable as Array<OptimizedInstruction>

        val cells : ByteArray = ByteArray(tapeSize)
        var pc : Int = 0
        var cp : Int = 0
        val zero : Byte = 0

        while(pc < instructions.size) {
            when(instructions[pc]) {
                is OptimizedInstruction.Add -> {
                    val addInstruction = instructions[pc] as OptimizedInstruction.Add
                    cells[cp] = (cells[cp] + addInstruction.addDelta).toByte()
                }

                is OptimizedInstruction.Move -> {
                    val moveInstruction = instructions[pc] as OptimizedInstruction.Move
                    cp += moveInstruction.moveDelta
                    if(cp < 0) {
                        throw Error("Memory underrun")
                    }
                    if(cp >= tapeSize) {
                        throw Error("Memory overrun")
                    }
                }
                is OptimizedInstruction.Print -> print(cells[cp].toUByte().toInt().toChar())

                is OptimizedInstruction.Read -> {
                    val c = System.`in`.read()
                    if(c == -1) throw Exception("End of input")
                    cells[cp] = c.toByte()
                }
                is OptimizedInstruction.LoopClose -> {
                    if(cells[cp] != zero) {
                        val instruction = instructions[pc] as OptimizedInstruction.LoopClose
                        pc = instruction.jumpTarget
                    }
                }
                is OptimizedInstruction.LoopOpen -> {
                    if(cells[cp] == zero) {
                        val instruction = instructions[pc] as OptimizedInstruction.LoopOpen
                        pc = instruction.jumpTarget
                    }
                }
            }
            pc++
        }
    }
}