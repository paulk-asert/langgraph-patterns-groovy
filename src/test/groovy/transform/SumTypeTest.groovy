package transform

import org.junit.jupiter.api.Test

class SumTypeTest {
    @Test
    void "check Sentiment example 1"() {
        var gcl = new GroovyClassLoader()
        var mySumTypeClass = gcl.parseClass('''
        import transform.SumType

        class Outer {
            @SumType(jsonProperty='kind', enumName='SentimentKind', variantHelper='toSentiment')
            interface Sentiment {
                Positive(double confidence)
                Negative(double confidence, String reason)
            }
        }
        ''')

        var inners = mySumTypeClass.declaredClasses*.toString()
        [
                'interface Outer$Sentiment',
                'class Outer$Positive',
                'class Outer$Negative',
                'class Outer$SentimentKind'
        ].every { signature -> assert inners.contains(signature) }

        var methods = mySumTypeClass.declaredClasses*.methods.sum()*.toString()
        assert methods.contains('public Outer$Sentiment Outer$SentimentKind.toSentiment()')

        var iface = mySumTypeClass.declaredClasses.find{ it.name == 'Outer$Sentiment' }
        assert iface.annotations.toString().contains('@com.fasterxml.jackson.annotation.JsonTypeInfo(property="kind",')

        var kindEnum = mySumTypeClass.declaredClasses.find{ it.name == 'Outer$SentimentKind' }
        assert kindEnum.enumConstants*.toString() == ['POSITIVE', 'NEGATIVE']
    }

    @Test
    void "check Sentiment example 2"() {
        var gcl = new GroovyClassLoader()
        var mySumTypeClass = gcl.parseClass('''
        import transform.SumType

        class Outer {
            @SumType(json=false)
            interface Sentiment {
                Positive(double confidence)
                Negative(double confidence, String reason)
            }
        }
        ''')

        var inners = mySumTypeClass.declaredClasses*.toString()
        [
                'interface Outer$Sentiment',
                'class Outer$Positive',
                'class Outer$Negative',
                'class Outer$SentimentKind'
        ].every { signature -> assert inners.contains(signature) }

        var methods = mySumTypeClass.declaredClasses*.methods.sum()*.toString()
        assert methods.contains('public Outer$Sentiment Outer$SentimentType.toVariant()')

        var iface = mySumTypeClass.declaredClasses.find{ it.name == 'Outer$Sentiment' }
        assert !iface.annotations.toString().contains('jackson')

        var kindEnum = mySumTypeClass.declaredClasses.find{ it.name == 'Outer$SentimentType' }
        assert kindEnum.enumConstants*.toString() == ['POSITIVE', 'NEGATIVE']
    }

    @Test
    void "check Sentiment example 3"() {
        var shell = new GroovyShell()
        var sentiment = shell.evaluate('''
        import transform.SumType

        class Outer {
            @SumType(json=false)
            interface Sentiment {
                Positive(double confidence)
                Negative(double confidence, String reason)
            }
            
            static Sentiment factory() {
                new Positive(confidence: 0.8)
            }
        }
        Outer.factory()
        ''')
        assert sentiment.class.name == 'Outer$Positive'
        assert sentiment.confidence == 0.8
    }
}