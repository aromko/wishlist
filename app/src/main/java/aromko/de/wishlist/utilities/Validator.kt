package aromko.de.wishlist.utilities

class Validator {
    companion object {
        fun checkUrl(url: String): Boolean {
            return !url.contains("(http|https)://".toRegex())
        }
    }
}