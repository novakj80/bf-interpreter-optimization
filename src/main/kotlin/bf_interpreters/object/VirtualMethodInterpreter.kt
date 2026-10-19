package bf_interpreters.`object`

class VirtualMethodInterpreter : bf_interpreters.BrainfuckInterpreter {

    class Opening(val lineNumber : Int, val linePosition : Int, val openInstructionIndex: Int)
    override fun compile(program: String): Any {
        val instructions = mutableListOf<Instruction>()

        val lines = program.split("\n")
        val openLoops = ArrayDeque<Opening>();

        for (li in 0..<lines.size) {
            for (c in 0..<lines[li].length) {
                when(lines[li][c]) {
                    '>' -> { instructions.add(Instruction.MoveRight()) }
                    '<' -> { instructions.add(Instruction.MoveLeft()) }
                    '+' -> { instructions.add(Instruction.Increment()) }
                    '-' -> { instructions.add(Instruction.Decrement()) }
                    '.' -> { instructions.add(Instruction.Print())}
                    ',' -> { instructions.add(Instruction.Read()) }
                    '[' -> {
                        val openInstruction = Instruction.LoopOpen()
                        val openLoop = Opening(li, c, instructions.size)
                        openLoops.add(openLoop)
                        instructions.add(openInstruction)
                    }
                    ']' -> {
                        if(openLoops.isEmpty()) throw Error("Unopened cycle - line ${li+1} character ${c+1}")
                        val opening = openLoops.removeLast()

                        val openingInstruction : Instruction.LoopOpen = instructions[opening.openInstructionIndex] as Instruction.LoopOpen
                        openingInstruction.jumpTarget = instructions.size

                        val closingInstruction = Instruction.LoopClose()
                        closingInstruction.jumpTarget = opening.openInstructionIndex - 1
                        instructions.add(closingInstruction)
                    }
                }
            }
        }

        if(openLoops.isNotEmpty()) {throw Error("Unclosed cycle - line ${lines.size} character ${lines.last().length}")
        }


        return instructions.toTypedArray()
    }

    override fun run(executable: Any, tapeSize: Int) {
        val instructions = executable as Array<Instruction>
        val ctx = Context(Array<UByte>(tapeSize) {0u}, 0, 0)
        while(ctx.pc < instructions.size) {
            instructions[ctx.pc].execute(ctx)
            ctx.pc += 1
        }
    }
}