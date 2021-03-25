package th.co.srichand.zebraprintkotlin

import android.graphics.Bitmap
import java.util.*


class ZPLconverter {
    private var blackLimit = 380
    private var total = 0
    private var widthBytes = 0
    private var compressHex = false
    fun convertFromImage(image: Bitmap, addHeaderFooter: Boolean): String {
        var hexAscii = createBody(image)
        if (compressHex) {
            hexAscii = encodeHexAscii(hexAscii)
        }

        //  String zplCode = "^GFA," + total + "," + total + "," + widthBytes + ", " + hexAscii;
        var zplCode = " $hexAscii"
        if (addHeaderFooter) {
            val header = "^XA ^FO50,50^GFA,$total,$total,$widthBytes, "
            val footer = "^FS" + "^XZ"
            zplCode = header + zplCode + footer
        }
        return zplCode
    }

    private fun createBody(bitmapImage: Bitmap): String {
        val sb = StringBuilder()
        val height = bitmapImage.height
        val width = bitmapImage.width
        var rgb: Int
        var red: Int
        var green: Int
        var blue: Int
        var index = 0
        var auxBinaryChar = charArrayOf('0', '0', '0', '0', '0', '0', '0', '0')
        widthBytes = width / 8
        widthBytes = if (width % 8 > 0) {
            (width / 8) + 1
        } else {
            width / 8
        }
        total = widthBytes * height
        for (h in 0 until height) {
            for (w in 0 until width) {
                rgb = bitmapImage.getPixel(w, h)
                red = rgb shr 16 and 0x000000FF
                green = rgb shr 8 and 0x000000FF
                blue = rgb and 0x000000FF
                var auxChar = '1'
                val totalColor = red + green + blue
                if (totalColor > blackLimit) {
                    auxChar = '0'
                }
                auxBinaryChar[index] = auxChar
                index++
                if (index == 8 || w == width - 1) {
                    sb.append(fourByteBinary(String(auxBinaryChar)))
                    auxBinaryChar = charArrayOf('0', '0', '0', '0', '0', '0', '0', '0')
                    index = 0
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun fourByteBinary(binaryStr: String): String {
        val decimal = binaryStr.toInt(2)
        return if (decimal > 15) {
            Integer.toString(decimal, 16).toUpperCase()
        } else {
            "0" + Integer.toString(decimal, 16).toUpperCase()
        }
    }

    private fun encodeHexAscii(code: String): String {
        val maxlinea = widthBytes * 2
        val sbCode = StringBuilder()
        val sbLinea = StringBuilder()
        var previousLine: String? = null
        var counter = 1
        var aux = code[0]
        var firstChar = false
        for (i in 1 until code.length) {
            if (firstChar) {
                aux = code[i]
                firstChar = false
                continue
            }
            if (code[i] == '\n') {
                if (counter >= maxlinea && aux == '0') {
                    sbLinea.append(",")
                } else if (counter >= maxlinea && aux == 'F') {
                    sbLinea.append("!")
                } else if (counter > 20) {
                    val multi20 = counter / 20 * 20
                    val resto20 = counter % 20
                    sbLinea.append(mapCode[multi20])
                    if (resto20 != 0) {
                        sbLinea.append(mapCode[resto20]).append(aux)
                    } else {
                        sbLinea.append(aux)
                    }
                } else {
                    sbLinea.append(mapCode[counter]).append(aux)
                }
                counter = 1
                firstChar = true
                if (sbLinea.toString() == previousLine) {
                    sbCode.append(":")
                } else {
                    sbCode.append(sbLinea.toString())
                }
                previousLine = sbLinea.toString()
                sbLinea.setLength(0)
                continue
            }
            if (aux == code[i]) {
                counter++
            } else {
                if (counter > 20) {
                    val multi20 = counter / 20 * 20
                    val resto20 = counter % 20
                    sbLinea.append(mapCode[multi20])
                    if (resto20 != 0) {
                        sbLinea.append(mapCode[resto20]).append(aux)
                    } else {
                        sbLinea.append(aux)
                    }
                } else {
                    sbLinea.append(mapCode[counter]).append(aux)
                }
                counter = 1
                aux = code[i]
            }
        }
        return sbCode.toString()
    }

    fun setCompressHex(compressHex: Boolean) {
        this.compressHex = compressHex
    }

    fun setBlacknessLimitPercentage(percentage: Int) {
        blackLimit = percentage * 768 / 100
    }

    companion object {
        private val mapCode: MutableMap<Int, String> = HashMap()
    }

    init {
        mapCode[1] = "G"
        mapCode[2] = "H"
        mapCode[3] = "I"
        mapCode[4] = "J"
        mapCode[5] = "K"
        mapCode[6] = "L"
        mapCode[7] = "M"
        mapCode[8] = "N"
        mapCode[9] = "O"
        mapCode[10] = "P"
        mapCode[11] = "Q"
        mapCode[12] = "R"
        mapCode[13] = "S"
        mapCode[14] = "T"
        mapCode[15] = "U"
        mapCode[16] = "V"
        mapCode[17] = "W"
        mapCode[18] = "X"
        mapCode[19] = "Y"
        mapCode[20] = "g"
        mapCode[40] = "h"
        mapCode[60] = "i"
        mapCode[80] = "j"
        mapCode[100] = "k"
        mapCode[120] = "l"
        mapCode[140] = "m"
        mapCode[160] = "n"
        mapCode[180] = "o"
        mapCode[200] = "p"
        mapCode[220] = "q"
        mapCode[240] = "r"
        mapCode[260] = "s"
        mapCode[280] = "t"
        mapCode[300] = "u"
        mapCode[320] = "v"
        mapCode[340] = "w"
        mapCode[360] = "x"
        mapCode[380] = "y"
        mapCode[400] = "z"
    }
}
