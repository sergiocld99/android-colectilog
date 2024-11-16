package cs10.apps.travels.tracer.pages.fab.tools

class LimitedSet<T>(private val maxSize: Int) : HashSet<T>() {

    override fun add(element: T): Boolean {
        if (this.size == maxSize) return false
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        for (e in elements) {
            if (!this.add(e)) return false
        }

        return true
    }

    fun isFull() = this.size == maxSize
}