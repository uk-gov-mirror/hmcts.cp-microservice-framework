package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.jmx.api.mbean.CommandRunMode.fromBoolean;

import uk.gov.justice.services.jmx.api.CommandNotFoundException;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;
import uk.gov.justice.services.jmx.command.CommandConverter;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private JmxCommandRunner jmxCommandRunner;

    @Inject
    private SystemCommandScanner systemCommandScanner;

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Inject
    private CommandConverter commandConverter;

    @Inject
    private Logger logger;


    @Override
    public UUID call(
            final String systemCommandName,
            final UUID commandRuntimeId,
            final String commandRuntimeString,
            final boolean guarded) {

        logger.info(format("Received System Command '%s'", systemCommandName));
        final CommandRunMode commandRunMode = fromBoolean(guarded);

        final StringBuilder logMessageBuilder = new StringBuilder("Running '").append(systemCommandName)
                .append("' in ").append(commandRunMode).append(" mode");

        final JmxCommandRuntimeParametersBuilder jmxCommandRuntimeParametersBuilder = new JmxCommandRuntimeParametersBuilder();

        if (commandRuntimeId != null) {
            logMessageBuilder.append(" with command-runtime-id '").append(commandRuntimeId).append("'");
            jmxCommandRuntimeParametersBuilder.withCommandRuntimeId(commandRuntimeId);
        }

        if (commandRuntimeString != null) {
            if (commandRuntimeId == null) {
                logMessageBuilder.append(" with");
            }  else {
                logMessageBuilder.append(" and");
            }

            logMessageBuilder.append(" command-runtime-string '").append(commandRuntimeString).append("'");
            jmxCommandRuntimeParametersBuilder.withCommandRuntimeString(commandRuntimeString);
        }

        logger.info(logMessageBuilder.toString());

        return jmxCommandRunner.run(
                systemCommandName,
                jmxCommandRuntimeParametersBuilder.build(),
                commandRunMode);
    }

    @Override
    public List<SystemCommandDetails> listCommands() {
        return systemCommandScanner.findCommands().stream()
                .map(commandConverter::toCommandDetails)
                .collect(toList());
    }

    @Override
    public SystemCommandStatus getCommandStatus(final UUID commandId) {
        return systemCommandStateBean
                .getCommandStatus(commandId)
                .orElseThrow(() -> new CommandNotFoundException(format("No SystemCommand found with id %s", commandId)));
    }
}
