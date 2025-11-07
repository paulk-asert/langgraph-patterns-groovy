package transform

import com.fasterxml.jackson.annotation.JsonTypeInfo
import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import groovy.transform.stc.POJO
import org.codehaus.groovy.antlr.EnumHelper
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.classgen.EnumVisitor
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static groovyjarjarasm.asm.Opcodes.*
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedInnerClass
import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static com.fasterxml.jackson.annotation.JsonTypeInfo.*

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class SumTypeTransform extends AbstractASTTransformation implements CompilationUnitAware {

    private static final ClassNode JsonTypeInfo_TYPE = ClassHelper.makeWithoutCaching(JsonTypeInfo, true)
    private static final ClassNode POJO_TYPE = ClassHelper.makeWithoutCaching(POJO, true)
    private static final ClassNode CS_TYPE = ClassHelper.makeWithoutCaching(CompileStatic, true)

    private CompilationUnit unit

    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode anno = nodes[0]
        AnnotatedNode parent = nodes[1]

        if (!(parent instanceof ClassNode)) return
        ClassNode iface = (ClassNode) parent
        makePojo(iface)

        String property = anno.getMember('jsonProperty')?.text ?: 'type'
        boolean json = !memberHasValue(anno, "json", false)
        String helperName = getMemberStringValue(anno, 'variantHelper', 'toVariant')

        if (json) {
            var jacksonAnno = new AnnotationNode(JsonTypeInfo_TYPE)
            jacksonAnno.setMember('use', propX(classX(Id), 'SIMPLE_NAME'))
            jacksonAnno.setMember('include', propX(classX(As), 'PROPERTY'))
            jacksonAnno.setMember('property', constX(property))
            iface.addAnnotation(jacksonAnno)
        }
        var all = iface.outerClass.innerClasses.toList()
        var variants = all.findAll{ it.implementsInterface(iface) }
        ClassNode enumNode = all.find { it.isEnum() }
        addGeneratedInnerClass(iface, enumNode)
        makePojo(enumNode)

        var placeholderMethods = []
        iface.methods.eachWithIndex { mn, idx ->
            if (mn.name.contains('init>')) return
            placeholderMethods << mn
            String enumConst = mn.name.toUpperCase()
            EnumHelper.addEnumConstant(enumNode, enumConst, null)
            var variant = variants.find{ it.name.endsWith(mn.name)}
            mn.parameters.each { p ->
                variant.addProperty(p.name, ACC_PUBLIC, p.type, nullX(), null, null)
            }
        }
        iface.methods.removeAll(placeholderMethods)
        variants.each { variant ->
            addGeneratedInnerClass(iface, variant)
            makePojo(variant)
        }

        var switchCases = variants.collect {
            int dollar = it.name.lastIndexOf('$') + 1
            caseS(fieldX(enumNode, it.name.substring(dollar).toUpperCase()), returnS(ctorX(it)))
        }
        var switchStmt = switchS(varX("this"), switchCases, EmptyStatement.INSTANCE)
        var helperMethod = new MethodNode(
                helperName,
                ACC_PUBLIC,
                iface,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                block(switchStmt)
        )
        enumNode.addMethod(helperMethod)
        new EnumVisitor(unit, source).visitClass(enumNode)

        var sealedNode = iface.addAnnotation(ClassHelper.SEALED_TYPE)
        sealedNode.setMember("permittedSubclasses", listX(variants.collect(ClassExpression::new)))
        iface.setPermittedSubclasses(variants)
        sealedNode.setNodeMetaData("permits", Boolean.TRUE)
    }

    private static void makePojo(ClassNode node) {
        node.addAnnotation(new AnnotationNode(POJO_TYPE))
        node.addAnnotation(new AnnotationNode(CS_TYPE))
    }

    @Override
    void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit
    }
}
