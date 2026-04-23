package uk.gov.justice.services.core.featurecontrol.local;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.File;
import java.net.URL;
import java.util.Optional;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class LocalFeatureFileLocator {


    @Inject
    private WildflyDeploymentDirectoryLocator wildflyDeploymentDirectoryLocator;

    @Inject
    private FileToUrlConverter fileToUrlConverter;

    @Inject
    private Logger logger;

    public Optional<URL> findLocalFeatureFileLocation(final String featureControlFileName) {

        final File deploymentDirectory = wildflyDeploymentDirectoryLocator.getDeploymentDirectory().toFile();

        if (deploymentDirectory.exists()) {

            final File file = new File(deploymentDirectory, featureControlFileName);

            if (file.exists()) {
                logger.warn(format("Feature control file found in wildfly deployment directory: '%s'", file.getAbsolutePath()));
                return of(fileToUrlConverter.toUrl(file));
            }
        }  else {
            logger.error(format("wildfly deployment dir '%s; does not exist", deploymentDirectory.getAbsolutePath()));
        }

        return empty();
    }
}
