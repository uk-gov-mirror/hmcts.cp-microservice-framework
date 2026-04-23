package uk.gov.justice.raml.jms.core;

import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import uk.gov.justice.services.generators.subscription.parser.EventSourcesFileParser;
import uk.gov.justice.services.generators.subscription.parser.EventSourcesFileParserFactory;
import uk.gov.justice.services.generators.subscription.parser.JmsUriToDestinationConverter;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.jms.core.JmsUriToDestinationTypeConverter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.jms.Destination;

import org.slf4j.Logger;

/**
 * In the past, when processing raml files for generating MDBs, the envent-sources.yaml files where jms_uri are defined were ignored.
 * This class is allowing to read any event-sources.yaml files in the classpath and trying to find the corresponding jms_uri
 * that is used to determine whether we are dealing with a topic or a queue.
 * Note that this is only used for event-processors only
 */
public class EventSourcesDestinationTypeFinder {

    private final Logger logger;
    private final JmsUriToDestinationTypeConverter uriToDestinationTypeConverter;
    private final JmsUriToDestinationConverter jmsUriToDestinationConverter;
    private final EventSourcesFileParserFactory eventSourceYamlParserFactory;

    EventSourcesDestinationTypeFinder(JmsUriToDestinationTypeConverter uriToDestinationTypeConverter,
                                      JmsUriToDestinationConverter jmsUriToDestinationConverter,
                                      EventSourcesFileParserFactory eventSourceYamlParserFactory,
                                      final Logger logger) {

        this.uriToDestinationTypeConverter = uriToDestinationTypeConverter;
        this.jmsUriToDestinationConverter = jmsUriToDestinationConverter;
        this.eventSourceYamlParserFactory = eventSourceYamlParserFactory;
        this.logger = logger;
    }

    /**
     * @param serviceComponent EVENT_PROCESSOR, etc
     * @param destinationName  the matching destination name
     * @return Queue, Topic or empty
     */
    public Optional<Class<? extends Destination>> findForEventProcessor(final String serviceComponent, final String destinationName) {

        try {

            if (isBlank(destinationName) || !uriToDestinationTypeConverter.isEventProcessor(serviceComponent)) {
                return empty();
            }

            final EventSourcesFileParser eventSourcesFileParser = eventSourceYamlParserFactory.create();
            final ArrayList<Path> list = new ArrayList<>();
            final List<EventSourceDefinition> eventSourceDefinitionList = eventSourcesFileParser.getEventSourceDefinitions(Path.of(""), list);
            if (eventSourceDefinitionList == null) {
                return empty();
            }

            final List<String> foundUris = eventSourceDefinitionList.stream().map(eventSourceDefinition -> eventSourceDefinition.getLocation().getJmsUri()).
                    filter(jmsUri -> destinationName.equalsIgnoreCase(uriToDestination(jmsUri))).toList();

            if (foundUris.isEmpty()) {
                return empty();
            }

            final Set<Optional<Class<? extends Destination>>> destinations = foundUris.stream().
                    map(uri -> uriToDestinationTypeConverter.convertForEventProcessor(serviceComponent, uri)).
                    filter(destinationType -> destinationType.isPresent()).
                    collect(Collectors.toSet());

            if (destinations.isEmpty()) {
                return empty();
            }

            if (destinations.size() > 1) {
                return empty();
            }

            final Optional<Class<? extends Destination>> finalDestination = destinations.stream().
                    findFirst().
                    get();

            return finalDestination;

        } catch (Exception e) {
            logger.warn("Failed in findForEventProcessor: {}", e.getMessage());
            return empty();
        }

    }

    private String uriToDestination(final String jmsUri) {
        try {
            return jmsUriToDestinationConverter.convert(jmsUri);
        } catch (Exception e) {
            return "";
        }
    }
}
