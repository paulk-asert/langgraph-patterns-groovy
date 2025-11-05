package patterns.routing

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.Ai
import com.embabel.agent.domain.io.UserInput
import com.fasterxml.jackson.annotation.JsonTypeInfo

@Agent(description = "Perform sentiment analysis")
class ClassificationAgent {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.SIMPLE_NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    sealed interface Sentiment {
    }

    static final class Positive implements Sentiment {
    }

    static final class Negative implements Sentiment {
    }

    record Response(String message) {
    }

    private enum SentimentType {
        POSITIVE, NEGATIVE

        Sentiment toSentiment() {
            switch (this) {
                case POSITIVE -> new Positive()
                case NEGATIVE -> new Negative()
            }
        }
    }

    @Action
    Sentiment classify(UserInput userInput, Ai ai) {
        ai
                .withAutoLlm()
                .createObject("""
                                Determine if the sentiment of the following text is positive or negative.
                                Text: "$userInput.content"
                                """, SentimentType)
                .toSentiment()
    }

    @Action
    Response encourage(UserInput userInput, Positive sentiment, Ai ai) {
        ai
                .withAutoLlm()
                .createObject("""
                        Generate an encouraging response to the following positive text:
                        $userInput.content
                        """, Response)
    }

    @Action
    Response help(UserInput userInput, Negative sentiment, Ai ai) {
        ai
                .withAutoLlm()
                .createObject("""
                        Generate a supportive response to the following negative text:
                        $userInput.content
                        """, Response)
    }

    @AchievesGoal(description = "Generate a response based on discerning sentiment in user input")
    @Action
    Response done(Response response) {
        response
    }

}
