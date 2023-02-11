package io.github.zjns.privacydefender

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class FontSdkClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv) {

    companion object {
        const val TARGET_CLASS = "com/founder/foundersdk/c/d"
        val EMPTY_RETURN_METHODS = arrayOf(
            "b", "c", "d", "g"
        )
        const val TARGET_DESCRIPTOR = "(Landroid/content/Context;)Ljava/lang/String;"
        const val TARGET_ACCESS = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (TARGET_ACCESS == access && TARGET_DESCRIPTOR == descriptor
            && EMPTY_RETURN_METHODS.contains(name)
        ) {
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
