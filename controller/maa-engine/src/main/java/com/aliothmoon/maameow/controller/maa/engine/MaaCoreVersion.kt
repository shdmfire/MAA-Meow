package com.aliothmoon.maameow.controller.maa.engine

import com.aliothmoon.maameow.controller.maa.engine.BuildConfig

/**
 * MAA Core 版本信息与兼容性检查。
 *
 * 构建期由 `.maaversion` 写入 [BuildConfig.MAA_CORE_VERSION]。
 * 未部署时（`setup_maa_core.py` 未运行）为空串，运行时放行。
 */
object MaaCoreVersion {

    /** 当前 MAA Core 版本（构建期写入，未部署时为空串） */
    val current: String get() = BuildConfig.MAA_CORE_VERSION

    /**
     * 检查当前版本是否满足最低要求
     * @param minimumRequired 最低要求版本（如 "v6.0.0-beta.1"）
     * @return true 表示满足要求，false 表示版本过低
     */
    fun meetsMinimumRequired(minimumRequired: String?): Boolean {
        if (minimumRequired.isNullOrBlank()) return true
        if (current.isBlank()) return true
        return runCatching {
            compareVersions(parseVersion(current), parseVersion(minimumRequired)) >= 0
        }.getOrDefault(true)
    }

    private fun parseVersion(version: String): VersionInfo {
        val cleanVersion = version.removePrefix("v").removePrefix("V")
        val parts = cleanVersion.split("-", limit = 2)
        val mainPart = parts[0]
        val preRelease = parts.getOrNull(1)
        val numbers = mainPart.split(".").map { it.toIntOrNull() ?: 0 }
        return VersionInfo(
            major = numbers.getOrElse(0) { 0 },
            minor = numbers.getOrElse(1) { 0 },
            patch = numbers.getOrElse(2) { 0 },
            preRelease = preRelease,
        )
    }

    private fun compareVersions(a: VersionInfo, b: VersionInfo): Int {
        if (a.major != b.major) return a.major - b.major
        if (a.minor != b.minor) return a.minor - b.minor
        if (a.patch != b.patch) return a.patch - b.patch
        return when {
            a.preRelease == null && b.preRelease == null -> 0
            a.preRelease == null -> 1
            b.preRelease == null -> -1
            else -> comparePreRelease(a.preRelease, b.preRelease)
        }
    }

    private fun comparePreRelease(a: String, b: String): Int {
        val aParts = a.split(".")
        val bParts = b.split(".")
        for (i in 0 until minOf(aParts.size, bParts.size)) {
            val aNum = aParts[i].toIntOrNull()
            val bNum = bParts[i].toIntOrNull()
            val cmp = when {
                aNum != null && bNum != null -> aNum.compareTo(bNum)
                aNum != null -> -1
                bNum != null -> 1
                else -> aParts[i].compareTo(bParts[i])
            }
            if (cmp != 0) return cmp
        }
        return aParts.size - bParts.size
    }

    private data class VersionInfo(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: String?,
    )
}
