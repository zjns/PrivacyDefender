package io.github.zjns.privacydefender

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

object Visitors {
    fun emptyStringMethodVisitor(mv: MethodVisitor): MethodVisitor {
        return object : MethodVisitor(Opcodes.ASM5, mv) {
            override fun visitCode() {
                mv.visitLdcInsn("")
                mv.visitInsn(Opcodes.ARETURN)
            }
        }
    }
}
