package com.alexjlockwood.twentyfortyeight.brightsdk.internal

import net.harawata.appdirs.AppDirsFactory
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
    private const val PACKAGE_NAME = "com.hexa.game.twentyfourtyeight"
    private const val VERSION = "1.0.0"
    private const val AUTHOR = "hexabrain"

    /**
     * Extract SDK resources to a persistent directory and add it to java.library.path.
     * Returns the directory path if successful, null otherwise.
     */
    fun extractSdkResources(): File? {
        return try {
            val sdkDir = getPersistentSdkDirectory()

            // Try to stop any running net_updater processes before extraction
            stopNetUpdaterProcesses()

            extractFilesToDirectory(sdkDir)
            addToLibraryPath(sdkDir)
            println("Bright Data: SDK resources extracted to: ${sdkDir.absolutePath}")
            println("Bright Data: Verifying extracted files:")
            SDK_FILES.forEach { fileName ->
                val file = File(sdkDir, fileName)
                println("  - $fileName: ${if (file.exists()) "✓ (${file.length()} bytes)" else "✗ MISSING"}")
            }
            sdkDir
        } catch (e: Exception) {
            println("Bright Data: Failed to extract SDK resources: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Attempt to stop any running net_updater processes (Windows only).
     * This prevents "file in use" errors when updating SDK files.
     */
    private fun stopNetUpdaterProcesses() {
        try {
            val os = System.getProperty("os.name").lowercase()
            if (!os.contains("windows")) {
                return
            }

            println("Bright Data: Checking for running net_updater processes...")

            // Use taskkill command on Windows
            val process = ProcessBuilder("taskkill", "/F", "/IM", "net_updater64.exe")
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("Bright Data: Stopped existing net_updater processes")
            } else {
                // Exit code 128 means process not found, which is fine
                println("Bright Data: No running net_updater processes found")
            }
        } catch (e: Exception) {
            // Not critical if this fails
            println("Bright Data: Could not check for running processes: ${e.message}")
        }
    }

    /**
     * Get (and create if needed) a persistent directory for SDK files.
     */
    private fun getPersistentSdkDirectory(): File {
        // Use AppDirs to find a standard local user data directory
        // Windows: C:\Users\<User>\AppData\Local\com.hexa.game.twentyfourtyeight\brightsdk
        val appDataDir = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, AUTHOR)
        val sdkDir = File(appDataDir, "brightsdk")
        
        if (!sdkDir.exists()) {
            sdkDir.mkdirs()
        }
        
        return sdkDir
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
     * Skips extraction if file already exists and net_updater is running (to avoid conflicts).
     * For config files, we always overwrite to ensure latest settings.
     */
    private fun extractResource(fileName: String, targetDir: File) {
        val resourcePath = "$RESOURCE_PATH$fileName"
        val inputStream = SdkResourceLoader::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Resource not found: $resourcePath")

        val outputFile = File(targetDir, fileName)

        // If file exists and it's an executable (not config), check if we should skip
        if (outputFile.exists() && (fileName.endsWith(".exe") || fileName.endsWith(".dll"))) {
            // Try to get resource size to compare
            val resourceSize = inputStream.available().toLong()
            val existingSize = outputFile.length()

            // If sizes match, skip extraction (file is likely the same)
            if (resourceSize == existingSize) {
                println("Bright Data: $fileName already exists with correct size, skipping extraction")
                inputStream.close()
                return
            }

            // Try to delete old file first (in case we're updating)
            try {
                outputFile.delete()
                println("Bright Data: Deleted old $fileName to update")
            } catch (e: Exception) {
                // If we can't delete (file in use), skip extraction and use existing file
                println("Bright Data: Cannot update $fileName (file in use), using existing version")
                inputStream.close()
                return
            }
        }

        // Extract the file
        try {
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
        } catch (e: Exception) {
            // If extraction fails but file exists, we can still proceed
            if (outputFile.exists()) {
                println("Bright Data: Failed to extract $fileName but existing file will be used: ${e.message}")
            } else {
                throw e
            }
        }
    }

    /**
     * Add the directory to java.library.path so JNA can find the DLL.
     */
    private fun addToLibraryPath(directory: File) {
        try {
            // Method 1: Set jna.library.path system property
            System.setProperty("jna.library.path", directory.absolutePath)
            println("Bright Data: Set jna.library.path to: ${directory.absolutePath}")

            // Method 2: Also try to add to PATH environment variable for Windows
            val path = System.getenv("PATH")
            val newPath = "${directory.absolutePath}${File.pathSeparator}$path"
            setEnv("PATH", newPath)
            println("Bright Data: Added to PATH environment variable")

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
