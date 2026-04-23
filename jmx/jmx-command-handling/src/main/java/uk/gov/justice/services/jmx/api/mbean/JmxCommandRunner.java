package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.command.SystemCommandLocator;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.UUID;

import jakarta.inject.Inject;

public class JmxCommandRunner {

    @Inject
    private SystemCommandLocator systemCommandLocator;

    @Inject
    private AsynchronousCommandRunner asynchronousCommandRunner;

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Inject
    private JmxCommandVerifier jmxCommandVerifier;

    public UUID run(
            final String systemCommandName,
            final JmxCommandRuntimeParameters jmxCommandRuntimeParameters,
            final CommandRunMode commandRunMode) {

        final SystemCommand systemCommand = systemCommandLocator
                .forName(systemCommandName)
                .orElseThrow(() -> new UnrunnableSystemCommandException(format("The system command '%s' is not supported on this context.", systemCommandName)));

        jmxCommandVerifier.verify(systemCommand, jmxCommandRuntimeParameters);

        if (commandRunMode.isGuarded()) {
            if (systemCommandStateBean.commandInProgress(systemCommand)) {
                throw new UnrunnableSystemCommandException(format("Cannot run system command '%s'. A previous call to that command is still in progress.", systemCommand.getName()));
            }
        }

        return asynchronousCommandRunner.run(systemCommand, jmxCommandRuntimeParameters);
    }
}
