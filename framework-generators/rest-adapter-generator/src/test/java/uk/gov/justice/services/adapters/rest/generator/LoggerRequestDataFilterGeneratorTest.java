package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.JavaFile.builder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.filter.LoggerRequestDataAdder;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raml.model.Raml;

@ExtendWith(MockitoExtension.class)
public class LoggerRequestDataFilterGeneratorTest {

    private static final String SERVICE_COMPONENT_NAME = "CUSTOM_API";

    @TempDir
    public File temporaryFolder;

    @InjectMocks
    private LoggerRequestDataFilterGenerator loggerRequestDataFilterGenerator;

    @Test
    public void shouldGenerateJmsLoggerMetadataInterceptor() throws Exception {

        final String packageName = "uk.gov.justice.api.filter";
        final String simpleName = "CustomApiRestExampleLoggerRequestDataFilter";
        final String baseUri = "http://localhost:8080/rest-adapter-generator/custom/api/rest/example";

        final CommonGeneratorProperties commonGeneratorProperties = mock(CommonGeneratorProperties.class);

        final Raml raml = mock(Raml.class);
        final GeneratorConfig generatorConfig = mock(GeneratorConfig.class);

        when(generatorConfig.getGeneratorProperties()).thenReturn(commonGeneratorProperties);
        when(commonGeneratorProperties.getServiceComponent()).thenReturn(SERVICE_COMPONENT_NAME);
        when(raml.getBaseUri()).thenReturn(baseUri);

        final List<TypeSpec> typeSpec = loggerRequestDataFilterGenerator.generateFor(
                raml,
                generatorConfig);

        final File outputDirectory = new File(temporaryFolder, "test-generation");
        outputDirectory.mkdirs();
        builder(packageName, typeSpec.get(0))
                .build()
                .writeTo(outputDirectory);

        File compilationOutputDir = new File(temporaryFolder, getClass().getSimpleName());
        compilationOutputDir.mkdirs();
        final Class<?> compiledClass = javaCompilerUtil().compiledClassOf(
                outputDirectory,
                compilationOutputDir,
                packageName,
                simpleName);

        testGeneratedClass(compiledClass);
    }

    private void testGeneratedClass(final Class<?> compiledClass) throws Exception {

        final Object customFilter = compiledClass.newInstance();
        final LoggerRequestDataAdder loggerRequestDataAdder = mock(LoggerRequestDataAdder.class);
        final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext containerResponseContext = mock(ContainerResponseContext.class);

        final Field loggerRequestDataAdderField = compiledClass.getDeclaredField("loggerRequestDataAdder");
        loggerRequestDataAdderField.setAccessible(true);
        loggerRequestDataAdderField.set(customFilter, loggerRequestDataAdder);

        final Method filterRequest = compiledClass.getMethod("filter", ContainerRequestContext.class);

        filterRequest.invoke(customFilter, containerRequestContext);

        verify(loggerRequestDataAdder).addToMdc(containerRequestContext, SERVICE_COMPONENT_NAME);

        final Method filterResponse = compiledClass.getMethod("filter", ContainerRequestContext.class, ContainerResponseContext.class);

        filterResponse.invoke(customFilter, containerRequestContext, containerResponseContext);

        verify(loggerRequestDataAdder).clearMdc();
    }
}