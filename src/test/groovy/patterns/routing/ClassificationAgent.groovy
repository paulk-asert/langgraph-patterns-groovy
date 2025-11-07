package patterns.routing

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.Ai
import com.embabel.agent.domain.io.UserInput
import transform.SumType

@Agent(description = 'Perform sentiment analysis')
class ClassificationAgent {

    @SumType(variantHelper = 'toSentiment')
    interface Sentiment {
        Positive()
        Negative()
    }

    record Response(String message) {
    }

    @Action
    Sentiment classify(UserInput userInput, Ai ai) {
        ai.withAutoLlm()
            .createObject("""
                Determine if the sentiment of the following text is positive or negative.
                Text: "$userInput.content"
                """, SentimentType)
            .toSentiment()
    }

    @Action
    Response encourage(UserInput userInput, Positive sentiment, Ai ai) {
        ai.withAutoLlm()
            .createObject("""
                Generate an encouraging response to the following positive text:
                $userInput.content
                """, Response)
    }

    @Action
    Response help(UserInput userInput, Negative sentiment, Ai ai) {
        ai.withAutoLlm()
            .createObject("""
                Generate a supportive response to the following negative text:
                $userInput.content
                """, Response)
    }

    @AchievesGoal(description = 'Generate a response based on discerning sentiment in user input')
    @Action
    Response done(Response response) {
        response
    }

}
