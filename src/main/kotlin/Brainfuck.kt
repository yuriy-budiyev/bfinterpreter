/*
 * MIT License
 *
 * Copyright (c) 2026 Yuriy Budiyev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yuriybudiyev.brainfuck

fun runBrainfuck(code: String) {
    evaluateIR(createIR(code))
}

private fun createIR(input: String): List<Operation> {
    val lexer = Lexer(input)
    val operations = ArrayList<Operation>()
    val addresses = IntStack()
    var operator: Operator? = lexer.next()
    while (operator != null) {
        when (operator) {
            Operator.MoveNext,
            Operator.MovePrevious,
            Operator.Increment,
            Operator.Decrement,
            Operator.Write,
            Operator.Read -> {
                var operand = 1
                var nextOperator: Operator? = lexer.next()
                while (nextOperator == operator) {
                    operand++
                    nextOperator = lexer.next()
                }
                operations.add(
                    Operation(
                        operator,
                        operand,
                    ),
                )
                operator = nextOperator
            }
            Operator.JumpZero -> {
                addresses.push(operations.size)
                operations.add(
                    Operation(
                        Operator.JumpZero,
                        0,
                    ),
                )
                operator = lexer.next()
            }
            Operator.JumpNonZero -> {
                if (addresses.size == 0) {
                    throw IllegalStateException("Unbalanced loop")
                }
                val address = addresses.pop()
                operations.add(
                    Operation(
                        Operator.JumpNonZero,
                        address + 1,
                    ),
                )
                operations[address].operand = operations.size
                operator = lexer.next()
            }
        }
    }
    if (addresses.size != 0) {
        throw IllegalStateException("Unbalanced loop")
    }
    return operations
}

private fun evaluateIR(operations: List<Operation>) {
    val memory = StringBuilder()
    memory.append('\u0000')
    var head = 0
    var operationPointer = 0
    while (operationPointer < operations.size) {
        val operation = operations[operationPointer]
        when (operation.operator) {
            Operator.MoveNext -> {
                head += operation.operand
                while (head >= memory.length) {
                    memory.append('\u0000')
                }
                operationPointer++
            }
            Operator.MovePrevious -> {
                if (head < operation.operand) {
                    throw IllegalStateException("Memory underflow")
                }
                head -= operation.operand
                operationPointer++
            }
            Operator.Increment -> {
                memory[head] += operation.operand
                operationPointer++
            }
            Operator.Decrement -> {
                memory[head] -= operation.operand
                operationPointer++
            }
            Operator.Write -> {
                repeat(operation.operand) {
                    print(memory[head])
                }
                operationPointer++
            }
            Operator.Read -> {
                repeat(operation.operand) {
                    val line = readln()
                    if (line.isEmpty()) {
                        memory[head] = '\u0000'
                    } else {
                        memory[head] = line[0]
                    }
                }
                operationPointer++
            }
            Operator.JumpZero -> {
                if (memory[head] == '\u0000') {
                    operationPointer = operation.operand
                } else {
                    operationPointer++
                }
            }
            Operator.JumpNonZero -> {
                if (memory[head] != '\u0000') {
                    operationPointer = operation.operand
                } else {
                    operationPointer++
                }
            }
        }
    }
}

private enum class Operator {

    MoveNext,
    MovePrevious,
    Increment,
    Decrement,
    Write,
    Read,
    JumpZero,
    JumpNonZero,
}

private class Operation(
    val operator: Operator,
    var operand: Int,
)

private class Lexer(private val input: String) {

    fun next(): Operator? {
        while (index < input.length) {
            return when (input[index++]) {
                '>' -> Operator.MoveNext
                '<' -> Operator.MovePrevious
                '+' -> Operator.Increment
                '-' -> Operator.Decrement
                '.' -> Operator.Write
                ',' -> Operator.Read
                '[' -> Operator.JumpZero
                ']' -> Operator.JumpNonZero
                else -> continue
            }
        }
        return null
    }

    private var index: Int = 0
}

private class IntStack {

    var size: Int = 0
        private set

    fun pop(): Int =
        data[--size]

    fun push(value: Int) {
        if (size < data.size) {
            data[size] = value
        } else {
            val oldData = data
            val newData = IntArray(oldData.size * 2)
            System.arraycopy(
                oldData,
                0,
                newData,
                0,
                oldData.size,
            )
            newData[size] = value
            data = newData
        }
        size++
    }

    private var data: IntArray = IntArray(16)
}
