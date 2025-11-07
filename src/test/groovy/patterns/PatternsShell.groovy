package patterns

import com.embabel.agent.api.common.autonomy.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.domain.io.UserInput
import patterns.promptchaining.BlogTitler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class PatternsShell {
    private AgentPlatform agentPlatform

    PatternsShell(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform
    }

    @ShellMethod('Generate blog titles')
    String blogs(@ShellOption(help = 'topic', defaultValue = '''
            LangGraph introduces a graph-based paradigm for building LLM-powered agents.
            It allows developers to create modular, debuggable, and reliable agent workflows
            using nodes, edges, and state passing.
            While this is an obvious approach, Embabel's GOAP planning is far superior.
            ''') String topic) {
        BlogTitler.BlogTitles blogs = AgentInvocation.create(agentPlatform, BlogTitler.BlogTitles)
                .invoke(new UserInput(topic))
        format(blogs)
    }

    private String format(Object result) {
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result)
    }
}
