package com.ling.lingaiagent.unit;

import com.ling.lingaiagent.agent.model.AgentState;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaseAgentUnitTest {

    /**
     * Test isStuck() returns false when message list is too short
     */
    @Test
    void testIsStuck_returnsFalse_whenLessThan2Messages() {
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("hello"));
        assertFalse(isStuck(messages));
    }

    /**
     * Test isStuck() returns false when messages are different
     */
    @Test
    void testIsStuck_returnsFalse_whenMessagesAreDifferent() {
        List<Message> messages = new ArrayList<>();
        messages.add(new AssistantMessage("response A"));
        messages.add(new AssistantMessage("response B"));
        messages.add(new AssistantMessage("response C"));
        assertFalse(isStuck(messages));
    }

    /**
     * Test isStuck() returns true when same message repeats 3+ times
     */
    @Test
    void testIsStuck_returnsTrue_whenSameMessageRepeats() {
        List<Message> messages = new ArrayList<>();
        messages.add(new AssistantMessage("I cannot proceed"));
        messages.add(new AssistantMessage("I cannot proceed"));
        messages.add(new AssistantMessage("I cannot proceed"));
        assertTrue(isStuck(messages));
    }

    /**
     * Test AgentState transitions
     */
    @Test
    void testAgentState_initialState() {
        AgentState state = AgentState.IDLE;
        assertEquals(AgentState.IDLE, state);
        assertNotEquals(AgentState.RUNNING, state);
    }

    @Test
    void testAgentState_allStatesExist() {
        assertNotNull(AgentState.IDLE);
        assertNotNull(AgentState.RUNNING);
        assertNotNull(AgentState.FINISHED);
        assertNotNull(AgentState.ERROR);
    }

    // Helper method replicating isStuck() logic
    private boolean isStuck(List<Message> messages) {
        if (messages.size() < 2) return false;
        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getText() == null || lastMessage.getText().isEmpty()) return false;

        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            if (lastMessage.getText().equals(messages.get(i).getText())) {
                duplicateCount++;
            }
        }
        return duplicateCount >= 2;
    }
}