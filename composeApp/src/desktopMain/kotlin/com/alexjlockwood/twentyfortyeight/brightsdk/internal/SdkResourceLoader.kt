package com.alexjlockwood.twentyfortyeight.brightsdk.internal

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

/**
 * Helper class to extract Bright Data SDK resources (DLLs and config) from JAR to filesystem.
 * JNA requires native libraries to be on the filesystem, not inside JAR.
 */
object SdkResourceLoader {

    private val SDK_FILES = listOf(
        "lum_sdk64.dll",
        "net_updater64.exe",
        "brd_config.json"
    )

    private const val RESOURCE_PATH = "/brightsdk/"

    /**
     * Extract SDK resources to a temp directory and add it to java.library.path.
     * Returns true if successful, false otherwise.
     */
    fun extractSdkResources(): Boolean {
        return try {
            val tempDir = createTempSdkDirectory()
            extractFilesToDirectory(tempDir)
            addToLibraryPath(tempDir)
            println("Bright Data: SDK resources extracted to: ${tempDir.absolutePath}")
            true
        } catch (e: Exception) {
            println("Bright Data: Failed to extract SDK resources: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Create a temporary directory for SDK files.
     */
    private fun createTempSdkDirectory(): File {
        val tempDir = Files.createTempDirectory("bright_sdk_").toFile()
        tempDir.deleteOnExit()
        return tempDir
    }

    /**
     * Extract all SDK files from resources to the target directory.
     */
    private fun extractFilesToDirectory(targetDir: File) {
        for (fileName in SDK_FILES) {
            extractResource(fileName, targetDir)
        }
    }

    /**
     * Extract a single resource file to the target directory.
     */
    private fun extractResource(fileName: String, targetDir: File) {
        val resourcePath = "$RESOURCE_PATH$fileName"
        val inputStream = SdkResourceLoader::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Resource not found: $resourcePath")

        val outputFile = File(targetDir, fileName)
        outputFile.deleteOnExit()

        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        // Make executables executable on Unix-like systems
        if (fileName.endsWith(".exe") || fileName.endsWith(".dll")) {
            outputFile.setExecutable(true)
        }

        println("Bright Data: Extracted $fileName to ${outputFile.absolutePath}")
    }

    /**
     * Add the directory to java.library.path so JNA can find the DLL.
     */
    private fun addToLibraryPath(directory: File) {
        try {
            // Method 1: Set jna.library.path system property
            System.setProperty("jna.library.path", directory.absolutePath)

            // Method 2: Also try to add to PATH environment variable for Windows
            val path = System.getenv("PATH")
            val newPath = "${directory.absolutePath}${File.pathSeparator}$path"
            setEnv("PATH", newPath)

        } catch (e: Exception) {
            println("Bright Data: Warning - couldn't modify library path: ${e.message}")
        }
    }

    /**
     * Attempt to modify environment variables at runtime (best effort).
     */
    private fun setEnv(key: String, value: String) {
        try {
            val processEnvironment = Class.forName("java.lang.ProcessEnvironment")
            val theEnvironmentField = processEnvironment.getDeclaredField("theEnvironment")
            theEnvironmentField.isAccessible = true

            @Suppress("UNCHECKED_CAST")
            val env = theEnvironmentField.get(null) as MutableMap<String, String>
            env[key] = value

            val theCaseInsensitiveEnvironmentField = processEnvironment.getDeclaredField("theCaseInsensitiveEnvironment")
            theCaseInsensitiveEnvironmentField.isAccessible = true

            @Suppress("UNCHECKED_CAST")
            val ciEnv = theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
            ciEnv[key] = value
        } catch (e: Exception) {
            // This is best-effort, so ignore failures
        }
    }
}
