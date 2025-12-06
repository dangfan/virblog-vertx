package fan.dang.wcfont

import com.google.typography.font.sfntly.Font
import com.google.typography.font.sfntly.FontFactory
import com.google.typography.font.sfntly.table.core.CMapTable
import com.google.typography.font.sfntly.Tag
import com.google.typography.font.tools.conversion.woff.WoffWriter
import com.google.typography.font.tools.subsetter.HintStripper
import com.google.typography.font.tools.subsetter.RenumberingSubsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

class FontService(private val config: AppConfig) {
    private val fontFactory = FontFactory.getInstance()
    private val woffWriter = WoffWriter()
    private val mutex = Mutex()

    init {
        // Ensure output directory exists
        File(config.fontOutputPath).mkdirs()
    }

    suspend fun subsetFont(family: String, content: String): String = withContext(Dispatchers.IO) {
        val hash = md5Hash(family + content)
        val filename = "$hash.woff"
        val outputFile = File(config.fontOutputPath, filename)

        // Use mutex to prevent concurrent subsetting of the same font
        mutex.withLock {
            if (!outputFile.exists()) {
                val inputFile = File(config.fontInputPath, "$family.ttf")
                if (!inputFile.exists()) {
                    throw IllegalArgumentException("Font file not found: $family.ttf")
                }
                createSubset(inputFile, outputFile, content)
            }
        }

        filename
    }

    private fun createSubset(inputFile: File, outputFile: File, content: String) {
        val fonts: Array<Font> = FileInputStream(inputFile).use { input ->
            fontFactory.loadFonts(input)
        }

        if (fonts.isEmpty()) {
            throw IllegalArgumentException("Could not load font from ${inputFile.name}")
        }

        val font = fonts[0]

        // Get unique code points from content
        val codePoints = content.codePoints().distinct().sorted().toArray().toMutableList()

        // Remove hinting to reduce file size
        val hintStripper = HintStripper(font, fontFactory)
        val strippedFont = hintStripper.subset().build()

        val cmapTable = strippedFont.getTable(Tag.cmap) as CMapTable?
            ?: throw IllegalArgumentException("Font missing cmap table")
        val cmap = cmapTable.cmap(CMapTable.CMapId.WINDOWS_BMP)
            ?: cmapTable.cmap(CMapTable.CMapId.WINDOWS_UCS4)
            ?: cmapTable.cmap(CMapTable.CMapId.MAC_ROMAN)
            ?: throw IllegalArgumentException("Font has no usable cmap")

        // Map requested code points to glyph IDs, skip missing to avoid out-of-bounds
        val glyphIds = mutableSetOf<Int>()
        glyphIds.add(0) // .notdef
        codePoints.forEach { cp ->
            val gid = cmap.glyphId(cp)
            if (gid > 0) glyphIds.add(gid)
        }

        if (glyphIds.size <= 1) {
            throw IllegalArgumentException("No glyphs found in font for requested content")
        }

        // Create the subset
        val subsetter = RenumberingSubsetter(strippedFont, fontFactory)
        subsetter.setCMaps(listOf(CMapTable.CMapId.WINDOWS_BMP), CMapTable.CMapId.WINDOWS_BMP.platformId())
        subsetter.setGlyphs(glyphIds.toList())

        val subsetFont = subsetter.subset().build()

        // Write as WOFF
        FileOutputStream(outputFile).use { output ->
            val woffData = woffWriter.convert(subsetFont)
            val bytes = ByteArray(woffData.length())
            woffData.readBytes(0, bytes, 0, woffData.length())
            output.write(bytes)
        }
    }

    private fun md5Hash(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
