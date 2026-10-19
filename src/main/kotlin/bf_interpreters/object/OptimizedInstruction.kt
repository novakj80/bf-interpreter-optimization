package bf_interpreters.`object`

sealed class OptimizedInstruction {

class Move(var moveDelta: Int): OptimizedInstruction()

    class Add(var addDelta: Int) : OptimizedInstruction() {
    }

class Read : OptimizedInstruction() {}

class Print : OptimizedInstruction() {}

class LoopOpen : OptimizedInstruction() {
    var jumpTarget: Int = 0
}

class LoopClose : OptimizedInstruction() {
    var jumpTarget: Int = 0
}
}