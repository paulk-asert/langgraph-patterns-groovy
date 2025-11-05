package patterns.promptchaining

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.prompt.persona.Actor
import com.embabel.common.ai.model.LlmOptions

@Agent(description = 'Blog Titler Agent')
class BlogTitler {

    private final Actor<?> techWriter = new Actor<>('''
            You are an expert technical writer. Always give clear,
            concise, and straight-to-the-point answers.
            ''',
            LlmOptions.withAutoLlm())

    record Topics(
            List<String> topics
    ) {
    }

    record TopicTitles(
            String topic,
            List<String> titles
    ) {
    }

    record BlogTitles(
            List<TopicTitles> topicTitles
    ) {
    }

    @Action
    Topics extractTopics(UserInput userInput, Ai ai) {
        techWriter.promptRunner(ai)
                .creating(Topics)
                .fromPrompt("""
                        Extract 1-3 key topics from the following text:
                        $userInput.content
                        """)
    }

    @Action
    @AchievesGoal(description='Generate Titles for Topics')
    BlogTitles generateBlogTitles(Topics topics, OperationContext context) {
        var titles = context.parallelMap(
                topics.topics(),
                10,
                topic -> techWriter.promptRunner(context)
                .creating(TopicTitles)
                .fromPrompt("""
                        Generate two catchy blog titles for this topic:
                        $topic
                        """))
        new BlogTitles(titles)
    }

}
