# Brainfuck Interpreter Benchmarks

A study in dispatch architecture and JVM performance, written in Kotlin.

Five interpreters for the [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) esoteric language,
each exploring a different approach to instruction dispatch and representation —
benchmarked on a Mandelbrot set fractal renderer (http://esoteric DOT sange DOT fi/brainfuck/utils/mandelbrot/).

## Interpreters

### Baseline (1:1 mapping to Brainfuck commands)

| Name | Dispatch strategy |
|---|---|
| `VirtualMethodInterpreter` | Each instruction is an object with a virtual `execute()` method |
| `ObjectInterpreter` | Instructions stored as `sealed class`; dispatched via exhaustive `when` |
| `BytecodeInterpreter` | Instructions encoded as integers in an `IntArray`; dispatched via `when` on opcode |

All three pre-compute jump targets for `[` and `]` at parse time,
avoiding linear scan during execution.

### Optimized (run-length encoded)

`OptimizedObjectInterpreter` and `OptimizedBytecodeInterpreter` extend their baseline
counterparts with a parse-time pass that merges consecutive identical `+`, `-`, `<`, `>`
instructions into a single operation with a count argument.

## Results

Benchmarked on JVM (Kotlin), averaged over 10 runs.

| Interpreter | Time (ms) | Speedup vs. baseline |
|---|---|---|
| VirtualMethodInterpreter | 33 925 ± 650 | 1.0× |
| ObjectInterpreter | 19 426 ± 336 | 1.7× |
| BytecodeInterpreter | 11 999 ± 178 | 2.8× |
| OptimizedObjectInterpreter | 6 097 ± 104 | 5.6× |
| **OptimizedBytecodeInterpreter** | **5 379 ± 119** | **6.3×** |

## A Surprising Finding

After adding run-length encoding, the bytecode interpreter was
*slower* than the sealed-class variant — the opposite of the expected result.

The cause: the bytecode program was originally stored as `Array<Int>`,
which the JVM compiles to `Integer[]` (boxed objects on the heap).
Every opcode fetch incurred a pointer dereference and potential cache miss.

Replacing `Array<Int>` with `IntArray` (primitive `int[]`) and
`Array<UByte>` with `ByteArray` for the tape restored the expected ordering
and confirmed the hypothesis.

This is a concrete example of a JVM pitfall that is easy to miss:
generic collections silently box primitive types, with measurable performance consequences.

## Project structure

```
src/main/kotlin/bf_interpreters
  BFInterpreter.kt          # Common interface
  object/Context.kt                # Interpreter state (tape, pointers)
  object/Instruction.kt            # Sealed class IR
  object/VirtualMethodInterpreter.kt
  object/ObjectInterpreter.kt
  bytecode/BytecodeInterpreter.kt
  object/OptimizedObjectInterpreter.kt
  bytecode/OptimizedBytecodeInterpreter.kt
  Benchmark.kt                   # Benchmark runner
```

## Running

```bash
./gradlew run --args="mandelbrot.b"
```
