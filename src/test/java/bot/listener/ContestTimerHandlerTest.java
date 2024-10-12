package bot.listener;

import bot.api.ApiCaller;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContestTimerHandlerTest {

    @Mock
    private ApiCaller apiCallerMock;

    @Mock
    private ButtonInteractionEvent eventMock;

    @Mock
    private Message messageMock;

    private ContestTimerHandler contestTimerHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contestTimerHandler = new ContestTimerHandler(apiCallerMock);
    }

    @Test
    void testTimerAddedAfterConfirmButton() {
        // Arrange
        String contestId = "1234";
        String contestName = "[Test Contest]";
        String startTimeStr = "May 1, 2023, 10:00 AM +03:00";
        String durationStr = "2 hours 25 minutes";
        String participantsStr = "`user1`, `user2`";

        // Mock the event and message
        MessageChannelUnion channelUnionMock = mock(MessageChannelUnion.class);
        MessageCreateAction messageCreateActionMock = mock(MessageCreateAction.class);
        ReplyCallbackAction replyCallbackActionMock = mock(ReplyCallbackAction.class);

        when(channelUnionMock.sendMessageEmbeds(any(MessageEmbed.class), any(MessageEmbed[].class))).thenReturn(messageCreateActionMock);
        when(eventMock.getChannel()).thenReturn(channelUnionMock);
        when(eventMock.reply(anyString())).thenReturn(replyCallbackActionMock);
        when(replyCallbackActionMock.setEphemeral(anyBoolean())).thenReturn(replyCallbackActionMock);

        // Mock the embed
        when(messageMock.getEmbeds()).thenReturn(List.of(createMockEmbed(startTimeStr, durationStr, participantsStr, contestId, contestName)));
        when(eventMock.getMessage()).thenReturn(messageMock);
        when(eventMock.getComponentId()).thenReturn("confirm_contest");

        // Act
        contestTimerHandler.onButtonInteraction(eventMock);

        // Assert
        assertTrue(contestTimerHandler.activeTimers.containsKey(contestId), "The timer should be added to activeTimers");
        ScheduledExecutorService scheduler = contestTimerHandler.activeTimers.get(contestId);
        assertNotNull(scheduler, "The timer scheduler should not be null");

        // Clean up the scheduler
        scheduler.shutdownNow();
    }


    @Test
    void testContestTournamentResult() throws IOException {
        List<String> participants = Arrays.asList("user1", "user2");
        String contestId = "1234";
        String contestName = "Test Contest";

        // Mock the API call
        when(apiCallerMock.makeApiCall(anyString())).thenReturn(createMockApiResponse());

        // Call the method
        EmbedBuilder result = contestTimerHandler.contestTournamentResult(participants, contestId, contestName);

        // Assert the result
        assertNotNull(result);
        assertEquals("Contest Results: Test Contest", result.build().getTitle());
        assertEquals("[`user1`](https://codeforces.com/profile/user1)\t:trophy: **Champion!**", result.build().getFields().get(0).getValue());
        assertEquals("1", result.build().getFields().get(1).getValue());
        assertEquals("1", result.build().getFields().get(2).getValue());
        assertEquals("0", result.build().getFields().get(3).getValue());
        assertEquals("[`user2`](https://codeforces.com/profile/user2)\t:medal: **Top 3, impressive!**", result.build().getFields().get(4).getValue());
        assertEquals("2", result.build().getFields().get(5).getValue());
        assertEquals("1", result.build().getFields().get(6).getValue());
        assertEquals("1", result.build().getFields().get(7).getValue());
    }

    private MessageEmbed createMockEmbed(String startTimeStr, String durationStr, String participantsStr, String contestId, String contestName) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Contest Details");
        embedBuilder.addField("Virtual Start Time", startTimeStr, false);
        embedBuilder.addField("Duration", durationStr, false);
        embedBuilder.addField("Participants", participantsStr, false);
        embedBuilder.addField("Contest ID", contestId, false);
        embedBuilder.addField("Contest Name", contestName, false);
        return embedBuilder.build();
    }

    private String createMockApiResponse() {
        return "{\"status\":\"OK\",\"result\":{\"contest\":{\"id\":1234,\"name\":\"Test Contest\"},\"problems\":[],\"rows\":[{\"party\":{\"members\":[{\"handle\":\"user1\"}]},\"rank\":1,\"points\":100,\"problemResults\":[{\"points\":100,\"rejectedAttemptCount\":0}]},{\"party\":{\"members\":[{\"handle\":\"user2\"}]},\"rank\":2,\"points\":50,\"problemResults\":[{\"points\":50,\"rejectedAttemptCount\":1}]}]}}";
    }
}