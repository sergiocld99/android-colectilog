package cs10.apps.travels.tracer.pages.live.model

data class SwitcherText(val id: String, val text: String) {


    override fun equals(other: Any?): Boolean {
        if (other is SwitcherText){
            return other.id == this.id
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}