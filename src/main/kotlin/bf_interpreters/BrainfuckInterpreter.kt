package bf_interpreters

interface BrainfuckInterpreter {
    fun compile(program: String): Any
    fun run(executable: Any, tapeSize: Int = 30000): Any
}