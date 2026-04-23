package uk.gov.justice.services.test.utils.core.messaging;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;


@ExtendWith(MockitoExtension.class)
public class MessageProducerClientTest {

    @Mock
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @InjectMocks
    private MessageProducerClient messageProducerClient;

    @Test
    public void shouldCreateAndStartMessageProducer() throws Exception {

        final String topicName = "some-topic-name";

        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination = mock(Topic.class);
        final MessageProducer messageProducer = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenReturn(destination);
        when(session.createProducer(destination)).thenReturn(messageProducer);

        messageProducerClient.startProducer(topicName);

        final InOrder inOrder = inOrder(
                activeMQConnectionFactory,
                connection,
                session);

        inOrder.verify(activeMQConnectionFactory).setBrokerURL(queueUri());
        inOrder.verify(activeMQConnectionFactory).createConnection();
        inOrder.verify(connection).start();
        inOrder.verify(connection).createSession(false, AUTO_ACKNOWLEDGE);
        inOrder.verify(session).createTopic(topicName);
        inOrder.verify(session).createProducer(destination);

        assertThat(getValueOfField(messageProducerClient, "messageProducer", MessageProducer.class), is(messageProducer));
        assertThat(getValueOfField(messageProducerClient, "session", Session.class), is(session));
        assertThat(getValueOfField(messageProducerClient, "connection", Connection.class), is(connection));
        assertThat(getValueOfField(messageProducerClient, "topicName", String.class), is(topicName));
    }

    @Test
    public void shouldCloseEverythingCorrectly() throws Exception {

        final String topicName = "some-topic-name";

        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination = mock(Topic.class);
        final MessageProducer messageProducer = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenReturn(destination);
        when(session.createProducer(destination)).thenReturn(messageProducer);

        messageProducerClient.startProducer(topicName);

        messageProducerClient.close();

        verify(messageProducer).close();
        verify(connection).close();
        verify(session).close();
    }

    @Test
    public void shouldDoNothingWhenCallingStartWithTheSameTopicName() throws Exception {

        final String topicName = "some-topic-name";

        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination = mock(Topic.class);
        final MessageProducer messageProducer = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenReturn(destination);
        when(session.createProducer(destination)).thenReturn(messageProducer);

        messageProducerClient.startProducer(topicName);
        messageProducerClient.startProducer(topicName);
        messageProducerClient.startProducer(topicName);
        messageProducerClient.startProducer(topicName);

        verify(activeMQConnectionFactory, times(1)).setBrokerURL(queueUri());
        verify(activeMQConnectionFactory, times(1)).createConnection();
        verify(connection, times(1)).start();
        verify(connection, times(1)).createSession(false, AUTO_ACKNOWLEDGE);
        verify(session, times(1)).createTopic(topicName);
        verify(session, times(1)).createProducer(destination);
    }

    @Test
    public void shouldCloseAndRecreateMessageProducerIfStartingWithNewTopicName() throws Exception {

        final String topicName_1 = "some-topic-name-1";
        final String topicName_2 = "some-topic-name-2";

        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination_1 = mock(Topic.class);
        final Topic destination_2 = mock(Topic.class);
        final MessageProducer messageProducer_1 = mock(MessageProducer.class);
        final MessageProducer messageProducer_2 = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName_1)).thenReturn(destination_1);
        when(session.createProducer(destination_1)).thenReturn(messageProducer_1);

        messageProducerClient.startProducer(topicName_1);

        when(session.createTopic(topicName_2)).thenReturn(destination_2);
        when(session.createProducer(destination_2)).thenReturn(messageProducer_2);

        messageProducerClient.startProducer(topicName_2);

        verify(activeMQConnectionFactory, times(1)).setBrokerURL(queueUri());
        verify(activeMQConnectionFactory, times(1)).createConnection();
        verify(connection, times(1)).start();
        verify(connection, times(1)).createSession(false, AUTO_ACKNOWLEDGE);
        verify(session, times(1)).createTopic(topicName_1);
        verify(session, times(1)).createProducer(destination_1);
        verify(session, times(1)).createTopic(topicName_2);
        verify(session, times(1)).createProducer(destination_2);
        verify(messageProducer_1).close();

        assertThat(getValueOfField(messageProducerClient, "messageProducer", MessageProducer.class), is(messageProducer_2));
        assertThat(getValueOfField(messageProducerClient, "session", Session.class), is(session));
        assertThat(getValueOfField(messageProducerClient, "connection", Connection.class), is(connection));
        assertThat(getValueOfField(messageProducerClient, "topicName", String.class), is(topicName_2));
    }


    @Test
    public void shouldCloseEverythingOnException() throws Exception {

        final String topicName = "some-topic-name";
        final JMSException jmsException = new JMSException("Ooops");

        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenThrow(jmsException);

        final MessageProducerClientException messageProducerClientException = assertThrows(
                MessageProducerClientException.class,
                () -> messageProducerClient.startProducer(topicName));

        assertThat(messageProducerClientException.getMessage(), is("Failed to create message producer to topic: 'some-topic-name', queue uri: '" + queueUri() + "'"));
        assertThat(messageProducerClientException.getCause(), is(jmsException));

        verify(connection).close();
        verify(session).close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldSendJsonEnvelope() throws Exception {

        final String topicName = "some-topic-name";
        final String commandName = "some-command";
        final JsonEnvelope jsonEnvelope = createEnvelope(
                commandName,
                getJsonBuilderFactory().createObjectBuilder()
                        .add("propertyName", "value")
                        .build());

        final String envelopeJson = jsonEnvelope.toDebugStringPrettyPrint();

        final TextMessage message = mock(TextMessage.class);
        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination = mock(Topic.class);
        final MessageProducer messageProducer = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenReturn(destination);
        when(session.createProducer(destination)).thenReturn(messageProducer);

        messageProducerClient.startProducer(topicName);

        when(session.createTextMessage()).thenReturn(message);

        messageProducerClient.sendMessage(commandName, jsonEnvelope);

        verify(message).setText(envelopeJson);
        verify(message).setStringProperty("CPPNAME", commandName);
        verify(messageProducer).send(message);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldCloseAllOnMessageSendingFailure() throws Exception {

        final String topicName = "some-topic-name";
        final String commandName = "some-command";
        final JsonEnvelope jsonEnvelope = createEnvelope(
                commandName,
                getJsonBuilderFactory().createObjectBuilder()
                        .add("propertyName", "value")
                        .build());

        final JMSException jmsException = new JMSException("Ooops");
        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final Topic destination = mock(Topic.class);
        final MessageProducer messageProducer = mock(MessageProducer.class);

        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTopic(topicName)).thenReturn(destination);
        when(session.createProducer(destination)).thenReturn(messageProducer);

        messageProducerClient.startProducer(topicName);

        when(session.createTextMessage()).thenThrow(jmsException);

        final MessageProducerClientException messageProducerClientException = assertThrows(
                MessageProducerClientException.class,
                () -> messageProducerClient.sendMessage(commandName, jsonEnvelope));

        assertThat(messageProducerClientException.getMessage(), startsWith("Failed to send message. commandName: 'some-command'"));
        assertThat(messageProducerClientException.getCause(), is(jmsException));

        verify(connection).close();
        verify(session).close();
        verify(messageProducer).close();
    }
}
