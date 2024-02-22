package storage

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import storage.StringUtils.encodeHex

object HashUtils {

    private const val STREAM_BUFFER_LENGTH = 1024

    fun getCheckSumFromFile(digest: MessageDigest, filePath: String): String {
        val file = File(filePath)
        return getCheckSumFromFile(digest, file)
    }

    fun getCheckSumFromFile(digest: MessageDigest, file: File): String {
        val fis = FileInputStream(file)
        val byteArray = updateDigest(digest, fis).digest()
        fis.close()
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }

    fun getCheckSumFromBytes(digest: MessageDigest, bytes: ByteArray): String {
        val byteArray = bytes.inputStream().use { updateDigest(digest, it).digest() }
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }

    fun getCheckSumFromStream(digest: MessageDigest, stream: InputStream): String {
        val byteArray = updateDigest(digest, stream).digest()
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }

    /**
     * Reads through an InputStream and updates the digest for the data
     *
     * @param digest The MessageDigest to use (e.g. MD5)
     * @param data Data to digest
     * @return the digest
     */
    private fun updateDigest(digest: MessageDigest, data: InputStream): MessageDigest {
        val buffer = ByteArray(STREAM_BUFFER_LENGTH)
        var read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        while (read > -1) {
            digest.update(buffer, 0, read)
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        }
        return digest
    }

}
