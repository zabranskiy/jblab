package examples.kotlin

fun main(args : Array<String>) {
    println(args.any { str -> true })
    println("string1")
}

fun test(a : String, b : String?, i : Int?, j : Long, s : Short?): String? {
    return "string2"
}

fun Array<Int>.inversionTest1(s : Array<Int>, f : (Int) -> Int) {
    f(5)
    s.map(f)
    s.any { w -> false }
    this.inversionTest1(s, f)
}

fun String.inversionTest2(i : Int) {
    println(this + i)
}
