import com.sun.jna.Native
import com.sun.jna.Library
import java.io.File

fun main() {
    println("=== Testing Bright Data SDK DLL Loading ===")
    println()

    // Test 1: Check if DLL file exists in download folder
    val downloadDll = File("C:\\Users\\anshu\\Downloads\\bright_sdk_win-1.603.799\\lum_sdk64.dll")
    println("1. DLL in download folder:")
    println("   Path: ${downloadDll.absolutePath}")
    println("   Exists: ${downloadDll.exists()}")
    if (downloadDll.exists()) {
        println("   Size: ${downloadDll.length()} bytes")
    }
    println()

    // Test 2: Check resources folder
    val resourcesDll = File("composeApp\\src\\desktopMain\\resources\\brightsdk\\lum_sdk64.dll")
    println("2. DLL in resources folder:")
    println("   Path: ${resourcesDll.absolutePath}")
    println("   Exists: ${resourcesDll.exists()}")
    if (resourcesDll.exists()) {
        println("   Size: ${resourcesDll.length()} bytes")
    }
    println()

    // Test 3: Try to load DLL directly from download folder
    println("3. Attempting to load DLL from download folder...")
    System.setProperty("jna.library.path", "C:\\Users\\anshu\\Downloads\\bright_sdk_win-1.603.799")
    println("   Set jna.library.path to: ${System.getProperty("jna.library.path")}")

    try {
        interface TestLib : Library
        val lib = Native.load("lum_sdk64", TestLib::class.java)
        println("   ✓ SUCCESS! DLL loaded successfully")
    } catch (e: UnsatisfiedLinkError) {
        println("   ✗ FAILED to load DLL")
        println("   Error: ${e.message}")
        println()
        println("   This usually means:")
        println("   - Missing Visual C++ Redistributable (most common)")
        println("   - Missing .NET Framework 4.5+")
        println("   - DLL architecture mismatch (32-bit vs 64-bit)")
        println()
        println("   Stack trace:")
        e.printStackTrace()
    }

    println()
    println("4. System Information:")
    println("   OS: ${System.getProperty("os.name")}")
    println("   OS Version: ${System.getProperty("os.version")}")
    println("   OS Arch: ${System.getProperty("os.arch")}")
    println("   Java Version: ${System.getProperty("java.version")}")
    println("   Java Vendor: ${System.getProperty("java.vendor")}")
}
