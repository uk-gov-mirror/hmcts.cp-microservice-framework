package uk.gov.justice.services.adapter.rest.parameter;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidParameterCollectionBuilderFactory implements ParameterCollectionBuilderFactory {

    @Override
    public ParameterCollectionBuilder create() {

        final Logger logger = LoggerFactory.getLogger(ParameterCollectionBuilder.class);

        return new ValidParameterCollectionBuilder(logger);
    }
}