package fan.dang.blog.util

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * OpenCC library interface for Chinese conversion.
 * Requires libopencc to be installed on the system.
 */
interface OpenCCLibrary : Library {
    fun opencc_open(configFileName: String): Pointer
    fun opencc_convert_utf8(opencc: Pointer, input: String, length: Long): String
    fun opencc_close(opencc: Pointer)
}

object OpenCC {
    private var library: OpenCCLibrary? = null
    private var opencc: Pointer? = null

    init {
        try {
            library = Native.load("opencc", OpenCCLibrary::class.java)
            opencc = library?.opencc_open("s2t.json")
        } catch (e: Exception) {
            println("Warning: OpenCC library not available. Chinese conversion will be disabled.")
        }
    }

    fun simplifiedToTraditional(content: String): String {
        val lib = library ?: return content
        val cc = opencc ?: return content
        return try {
            lib.opencc_convert_utf8(cc, content, content.toByteArray().size.toLong())
        } catch (e: Exception) {
            content
        }
    }

    fun convertWithQuotes(content: String): String {
        var result = simplifiedToTraditional(content)
        result = result.replace('"', '「')
        result = result.replace('"', '」')
        result = result.replace('\'', '『')
        result = result.replace('\'', '』')
        return result
    }
}
