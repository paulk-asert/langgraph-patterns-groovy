package patterns.reflection

import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableBuilder
import com.embabel.agent.api.common.workflow.loop.TextFeedback
import com.embabel.agent.core.Agent
import com.embabel.agent.domain.io.UserInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DraftAndRefine {

    record Draft(
            String content
    ) {
    }

    @Bean
    Agent draftAndRefineAgent() {
        RepeatUntilAcceptableBuilder
                .returning(Draft)
                .consuming(UserInput)
                .withMaxIterations(7)
                .withScoreThreshold(.99)
                .repeating(tac -> {
                    tac.ai()
                            .withAutoLlm()
                            .withId("draft")
                            .createObject("""
                                            You are an assistant helping to complete the following task:
                                            
                                            Task:
                                            $tac.input.content
                                            
                                            Current Draft:
                                            ${tac.lastAttemptOr("no draft yet")}
                                            
                                            Feedback:
                                            ${tac.lastFeedbackOr("no feedback yet")}
                                            
                                            Instructions:
                                            - If there is no draft and no feedback, generate a clear and complete response to the task.
                                            - If there is a draft but no feedback, improve the draft as needed for clarity and quality.
                                            - If there is both a draft and feedback, revise the draft by incorporating the feedback directly.
                                            - Always produce a single, improved draft as your output.
                                            """,
                                    Draft)
                })
                .withEvaluator(tac -> {
                    tac.ai().withAutoLlm()
                            .withId("evaluate_draft")
                            .createObject("""
                                            Evaluating the following draft, based on the given task.
                                            Score it from 0.0 to 1.0 (best) and provide constructive feedback for improvement.
                                            
                                            Task:
                                            $tac.input.content
                                            
                                            Draft:
                                            $tac.resultToEvaluate
                                            """,
                                    TextFeedback)
                })
                .buildAgent("draft_and_refine_agent", "An agent that drafts and refines content")
    }

}
