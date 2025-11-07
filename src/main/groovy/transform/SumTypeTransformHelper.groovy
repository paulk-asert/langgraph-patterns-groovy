package transform

import org.codehaus.groovy.antlr.EnumHelper
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static groovyjarjarasm.asm.Opcodes.*

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class SumTypeTransformHelper implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        source?.AST?.classes?.each { ClassNode cn ->
            cn?.annotations?.each { AnnotationNode anno ->
                if (anno.classNode.name.endsWith('SumType')) {
                    int dollar = cn.unresolvedName.indexOf('$') + 1
                    String name = cn.unresolvedName.substring(dollar)
                    var outer = cn.outerClass
                    String enumName = anno.getMember('enumName')?.text ?: name + 'Type'
                    cn.methods.each {
                        var variant = new InnerClassNode(outer, "$outer.name\$$it.name", ACC_PUBLIC | ACC_FINAL | ACC_STATIC, ClassHelper.OBJECT_TYPE)
                        variant.addInterface(cn)
                        cn.module.addImport(it.name, variant)
                    }
                    ClassNode enumNode = EnumHelper.makeEnumNode(enumName, ACC_ENUM | ACC_PUBLIC, new ClassNode[0], outer)
                    cn.module.addImport(enumName, enumNode)
                }
            }
        }
    }

}
