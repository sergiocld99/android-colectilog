package cs10.apps.common.android

class Emoji {

    companion object {

        private fun getEmojiByUnicode(unicode: Int): String {
            return String(Character.toChars(unicode))
        }

        fun getHandEmoji() = getEmojiByUnicode(0x1F44B)
        fun getTrainEmoji() = getEmojiByUnicode(0x1F686)
        fun getCarEmoji() = getEmojiByUnicode(0x1F697)
        fun getBusEmoji() = getEmojiByUnicode(0x1F68D)
        fun getGlobeEmoji() = getEmojiByUnicode(0x1F30E)
        fun getClockEmoji() = getEmojiByUnicode(0x1F553)
    }
}