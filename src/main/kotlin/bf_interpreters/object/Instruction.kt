package bf_interpreters.`object`

sealed class Instruction {
    abstract fun execute(ctx: Context)


class MoveLeft : Instruction() {
    override fun execute(ctx: Context) {
        ctx.cp--
        if(ctx.cp < 0) {
            throw Exception("Memory underrun")
        }

    }
}
class MoveRight : Instruction() {
    override fun execute(ctx: Context) {
        ctx.cp++
        if(ctx.cp >= ctx.cells.size) {
            throw Exception("Memory overrun")
        }
    }
}

class Increment : Instruction() {
    override fun execute(ctx: Context) {
        ctx.cells[ctx.cp]++
    }
}

class Decrement : Instruction() {
    override fun execute(ctx: Context) {
        ctx.cells[ctx.cp]--
    }
}

class Read : Instruction() {
    override fun execute(ctx: Context) {
        val c = System.`in`.read()
        if(c == -1) throw Exception("End of input")
        ctx.cells[ctx.cp] = c.toUByte()
    }
}

class Print : Instruction() {
    override fun execute(ctx: Context) {
        print(ctx.cells[ctx.cp].toInt().toChar())
    }
}

class LoopOpen : Instruction() {
    var jumpTarget: Int = 0
    override fun execute(ctx: Context) {
        if(ctx.cells[ctx.cp] == 0.toUByte()) {
            ctx.pc = jumpTarget
        }
    }
}

class LoopClose : Instruction() {
    var jumpTarget: Int = 0
    override fun execute(ctx: Context) {
        if(ctx.cells[ctx.cp] != 0.toUByte()) {
            ctx.pc = jumpTarget
        }
    }
}
}