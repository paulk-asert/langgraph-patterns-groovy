package transform

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.*

/**
 * Marks an interface, trait, or class as a sum type / algebraic data type.
 *
 * Usage examples:
 *
 * <pre>
 * @SumType
 * interface Sentiment {
 *     Positive(double confidence)
 *     Negative(double confidence, String reason)
 * }
 * </pre>
 *
 * Is the same as:
 *
 * <pre>
 * @SumType(property='type', enumName='SentimentType', json=true, variantHelper='toVariant')
 * interface Sentiment {
 *     Positive(double confidence)
 *     Negative(double confidence, String reason)
 * }
 * </pre>
 *
 * But you can change as desired:
 *
 * <pre>
 * @SumType(jsonProperty='kind', enumName='SentimentKind', variantHelper='toSentiment')
 * interface Sentiment {
 *     Positive(double confidence)
 *     Negative(double confidence, String reason)
 * }
 * </pre>
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("transform.SumTypeTransform")
@interface SumType {
    /**
     * The name to use when generating jackson JSON annotations for serialization.
     * Ignored if "json" is false.
     */
    String jsonProperty() default "type"

    /**
     * To support "smart constructors". By default, it will be the parent class/interface plus "Type"
     */
    String enumName() default ""

    /**
     * Whether to generate jackson Json annotations for serialization.
     */
    boolean json() default false

    /**
     * The name of the helper method to support "smart constructors".
     */
    String variantHelper() default "toVariant"
}
