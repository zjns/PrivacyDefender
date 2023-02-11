package io.github.zjns.privacydefender

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class PrivacyDefenderTransform(private val config: PrivacyDefenderConfig) : Transform() {
    companion object {
        const val WEIBO = "weibo"
        const val FONT_SDK = "font_sdk"

        val TARGETS = mapOf(
            WEIBO to "${WeiboClassVisitor.TARGET_CLASS}.class",
            FONT_SDK to "${FontSdkClassVisitor.TARGET_CLASS}.class"
        )
    }

    override fun getName(): String = "PrivacyDefenderTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean = false

    override fun transform(transformInvocation: TransformInvocation) {
        if (PrivacyDefender.DEBUG) {
            println("[PrivacyDefender] PrivacyDefenderTransform start")
        }
        val outputProvider = transformInvocation.outputProvider
        outputProvider?.deleteAll()
        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach { dirInput ->
                handleDirectoryInputs(dirInput, outputProvider)
            }
            it.jarInputs.forEach { jarInput ->
                handleJarInputs(jarInput, outputProvider)
            }
        }
    }

    private fun handleDirectoryInputs(
        dirInput: DirectoryInput,
        outputProvider: TransformOutputProvider
    ) {
        if (dirInput.file.isDirectory) {
            val destDir = outputProvider.getContentLocation(
                dirInput.name,
                dirInput.contentTypes,
                dirInput.scopes,
                Format.DIRECTORY
            )
            FileUtils.copyDirectory(dirInput.file, destDir)
        }
    }

    private fun handleJarInputs(jarInput: JarInput, outputProvider: TransformOutputProvider) {
        val jarFile = JarFile(jarInput.file)
        val destFile = outputProvider.getContentLocation(
            jarInput.name,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        val entries = jarFile.entries().toList()
        val entry = entries.find {
            TARGETS.values.contains(it.name)
        }
        val needHandle = entry != null
                && ((entry.name == TARGETS[WEIBO] && config.hookWeibo)
                || (entry.name == TARGETS[FONT_SDK] && config.hookFontSdk))
        // 不需要处理，直接拷贝源文件
        if (!needHandle) {
            if (PrivacyDefender.DEBUG) {
                println("[PrivacyDefender] Found no need handle jar, name: ${jarInput.name}, path: ${jarInput.file.absolutePath}")
            }
            FileUtils.copyFile(jarInput.file, destFile)
            return
        }

        if (PrivacyDefender.DEBUG) {
            println("[PrivacyDefender] Found need handle jar, name: ${jarInput.name}, path: ${jarInput.file.absolutePath}")
        }
        val finalJarFile = File(jarInput.file.parentFile.absolutePath, "tempName.jar")
        if (finalJarFile.exists()) finalJarFile.delete()
        val finalJarOutput = JarOutputStream(FileOutputStream(finalJarFile))
        entries.forEach { jarEntry ->
            val entryName = jarEntry.name
            val zipEntry = ZipEntry(entryName)

            when (entryName) {
                "${WeiboClassVisitor.TARGET_CLASS}.class" -> {
                    val reader = ClassReader(jarFile.getInputStream(zipEntry))
                    val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
                    val weiboVisitor = WeiboClassVisitor(writer)
                    reader.accept(weiboVisitor, ClassReader.EXPAND_FRAMES)
                    finalJarOutput.putNextEntry(zipEntry)
                    finalJarOutput.write(writer.toByteArray())
                    finalJarOutput.closeEntry()
                }

                "${FontSdkClassVisitor.TARGET_CLASS}.class" -> {
                    val reader = ClassReader(jarFile.getInputStream(zipEntry))
                    val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
                    val fontVisitor = FontSdkClassVisitor(writer)
                    reader.accept(fontVisitor, ClassReader.EXPAND_FRAMES)
                    finalJarOutput.putNextEntry(zipEntry)
                    finalJarOutput.write(writer.toByteArray())
                    finalJarOutput.closeEntry()
                }

                else -> {
                    finalJarOutput.putNextEntry(zipEntry)
                    finalJarOutput.write(jarFile.getInputStream(zipEntry).readBytes())
                    finalJarOutput.closeEntry()
                }
            }
        }

        finalJarOutput.flush()
        finalJarOutput.close()
        jarFile.close()
        FileUtils.copyFile(finalJarFile, destFile)
        finalJarFile.delete()
    }
}
