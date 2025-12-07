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
    private val library: OpenCCLibrary? by lazy {
        try {
            val lib = Native.load("opencc", OpenCCLibrary::class.java) as OpenCCLibrary
            println("OpenCC library loaded successfully")
            lib
        } catch (e: Throwable) {
            println("Warning: OpenCC library not available (${e.message}). Chinese conversion will be disabled.")
            null
        }
    }

    private val opencc: Pointer? by lazy {
        try {
            library?.opencc_open("s2t.json")
        } catch (e: Throwable) {
            println("Warning: Failed to initialize OpenCC converter (${e.message})")
            null
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
        result = result.replace('“', '「')
        result = result.replace('”', '」')
        result = result.replace('‘', '『')
        result = result.replace('’', '』')
        return result
    }
}
