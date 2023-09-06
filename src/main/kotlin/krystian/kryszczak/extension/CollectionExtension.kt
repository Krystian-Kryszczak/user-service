package krystian.kryszczak.extension

import kotlin.math.min
import kotlin.random.Random

inline fun <reified T> MutableList<T>.takeRandom(n: Int) = List(min(n, size)) { removeAt(Random.nextInt(size)) }
