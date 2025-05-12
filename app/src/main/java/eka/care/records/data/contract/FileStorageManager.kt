package eka.care.records.data.contract

import java.io.File

interface FileStorageManager {
    /**
     * Save a file to the local storage and return the path of the saved file.
     * @param file The file to save.
     * @return The path of the saved file.
     */
    suspend fun saveFile(file: File): String

    /**
     * Delete the file at the given path. Return true if the file was deleted successfully.
     * @param path The path of the file to delete.
     * @return True if the file was deleted successfully, false otherwise.
     */
    suspend fun deleteFile(path: String): Boolean

    /**
     * Get the file at the given path. Return null if the file does not exist.
     * @param path The path of the file to get.
     * @return The file at the given path.
     */
    suspend fun getFile(path: String): java.io.File?

    /**
     * Generate a thumbnail for the file at the given path. Return the path of the generated thumbnail.
     * Return null if the thumbnail could not be generated.
     * Files can be of type image, or pdf as of now.
     * @param filePath The path of the file to generate a thumbnail for.
     * @return The path of the generated thumbnail.
     */
    suspend fun generateThumbnail(filePath: String): String?

    /**
        UnusedFiles will delete all files that are not referenced in the database.
        It will return the count of deleted files.
     **/
    /**
     * Cleanup unused files from the local storage. Return the count of deleted files.
     * @return The count of deleted files.
     */
    suspend fun cleanupUnusedFiles(): Result<Int> // Returns count of deleted files
}