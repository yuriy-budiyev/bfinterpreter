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

fun runBrainfuck(code: CharSequence) {
    val lexer = Lexer(code)
    val operations = ArrayList<Operation>()
    val addresses = ArrayDeque<Int>()
    var operator: Operator? = lexer.next()
    while (operator != null) {
        when (operator) {
            Operator.MoveNext,
            Operator.MovePrevious,
            Operator.Increment,
            Operator.Decrement,
            Operator.Write,
            Operator.Read -> {
                var value = 1
                var nextOperator: Operator? = lexer.next()
                while (nextOperator == operator) {
                    value++
                    nextOperator = lexer.next()
                }
                operations.add(
                    Operation(
                        operator,
                        value,
                    ),
                )
                operator = nextOperator
            }
            Operator.JumpZero -> {
                addresses.addFirst(operations.size)
                operations.add(
                    Operation(
                        Operator.JumpZero,
                        0,
                    ),
                )
                operator = lexer.next()
            }
            Operator.JumpNonZero -> {
                if (addresses.isEmpty()) {
                    throw IllegalStateException("Unbalanced loop")
                }
                val address = addresses.removeFirst()
                operations.add(
                    Operation(
                        Operator.JumpNonZero,
                        address + 1,
                    )
                )
                operations[address].value = operations.size
                operator = lexer.next()
            }
        }
    }
    if (addresses.isNotEmpty()) {
        throw IllegalStateException("Unbalanced loop")
    }

}

private enum class Operator(val symbol: Char) {

    MoveNext('>'),
    MovePrevious('<'),
    Increment('+'),
    Decrement('-'),
    Write('.'),
    Read(','),
    JumpZero('['),
    JumpNonZero(']'),
}

private data class Operation(
    val type: Operator,
    var value: Int,
)

private class Lexer(private val input: CharSequence) {

    fun next(): Operator? {
        while (index < input.length) {
            return when (input[index++]) {
                Operator.MoveNext.symbol -> Operator.MoveNext
                Operator.MovePrevious.symbol -> Operator.MovePrevious
                Operator.Increment.symbol -> Operator.Increment
                Operator.Decrement.symbol -> Operator.Decrement
                Operator.Write.symbol -> Operator.Write
                Operator.Read.symbol -> Operator.Read
                Operator.JumpZero.symbol -> Operator.JumpZero
                Operator.JumpNonZero.symbol -> Operator.JumpNonZero
                else -> continue
            }
        }
        return null
    }

    private var index: Int = 0
}

