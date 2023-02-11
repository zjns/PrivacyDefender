package io.github.zjns.privacydefender

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class WeiboClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv) {
    companion object {
        const val TARGET_CLASS = "com/weibo/ssosdk/MfpBuilder"
        val EMPTY_RETURN_METHODS = arrayOf(
            "getImei",
            "getMeid",
            "getMac",
            "getMacAddr",
            "getSerialNo",
            "getAndroidId",
            "getSsid",
            "getWifiBssid"
        )
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (EMPTY_RETURN_METHODS.contains(name)) {
            if (PrivacyDefender.DEBUG) {
                println(
                    "[PrivacyDefender] Found need empty string return method, class: ${
                        TARGET_CLASS.replace("/", ".")
                    }, name: $name, descriptor: $descriptor"
                )
            }
            return Visitors.emptyStringMethodVisitor(mv)
        }
        return mv
    }
}
