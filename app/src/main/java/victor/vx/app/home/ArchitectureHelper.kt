package victor.vx.app.home

import android.util.Log

class ArchitectureHelper {
    fun detectArchitecture(): String {
        val isArchitecture = System.getProperty("os.arch") ?: return ""

        val abi = if (isArchitecture.contains("arm")) {
            "arm"
        } else if (isArchitecture.contains("aarch64")) {
            "arm64"
        } else if (isArchitecture.contains("x86_64")) {
            "x86_64"
        } else {
            Log.e("RootHelper", "Unsupported architecture")
            return ""
        }
        return abi
    }
}