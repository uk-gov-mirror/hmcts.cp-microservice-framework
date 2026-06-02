package uk.gov.justice.raml.jms.interceptor;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.JavaFile.builder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.raml.jms.core.ClassNameFactory.EVENT_FILTER;
import static uk.gov.justice.raml.jms.core.ClassNameFactory.EVENT_VALIDATION_INTERCEPTOR;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.raml.jms.core.ClassNameFactory;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.messaging.jms.HeaderConstants;

import java.io.File;
import java.lang.reflect.Field;

import jakarta.jms.TextMessage;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventValidationInterceptorCodeGeneratorTest {

    @TempDir
    public File temporaryFolder;

    @InjectMocks
    private EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator;

    @Test
    public void shouldGenerateAWorkingEventValidationInterceptorThatUsesACustomEventFilter() throws Exception {

        final String packageName = "uk.gov.justice.api.interceptor.filter";
        final String simpleName = "MyCustomEventValidationInterceptor";

        final ClassName eventValidationInterceptorClassName = get(packageName, simpleName);
        final ClassName eventFilterClassName = get(MyCustomEventFilter.class);
        final ClassNameFactory classNameFactory = mock(ClassNameFactory.class);

        when(classNameFactory.classNameFor(EVENT_VALIDATION_INTERCEPTOR)).thenReturn(eventValidationInterceptorClassName);
        when(classNameFactory.classNameFor(EVENT_FILTER)).thenReturn(eventFilterClassName);

        final TypeSpec typeSpec = eventValidationInterceptorCodeGenerator.generate(
                classNameFactory);

        final File codeGenerationOutputDirectory = new File(temporaryFolder, "test-generation");
        final File compilationOutputDirectory = new File(temporaryFolder, "java-test-classes");
        codeGenerationOutputDirectory.mkdirs();
        compilationOutputDirectory.mkdirs();

        builder(packageName, typeSpec)
                .addStaticImport(get(HeaderConstants.class), "JMS_HEADER_CPPNAME")
                .build()
                .writeTo(codeGenerationOutputDirectory);

        final Class<?> compiledClass = javaCompilerUtil().compiledClassOf(
                codeGenerationOutputDirectory,
                compilationOutputDirectory,
                packageName,
                simpleName);

        nowTestTheGeneratedClass(compiledClass);
        nowTestTheFailureCase(compiledClass);

    }

    private void nowTestTheGeneratedClass(final Class<?> compiledClass) throws Exception {

        final String eventName = "an.event.name";

        final JsonSchemaValidationInterceptor eventValidationInterceptor = buildTheClassForTest(
                compiledClass,
                new MyCustomEventFilter(eventName));

        final TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(eventName);

        assertThat(eventValidationInterceptor.shouldValidate(textMessage), is(true));
    }

    private void nowTestTheFailureCase(final Class<?> compiledClass) throws Exception {

        final String eventName = "an.event.name";
        final String messageName = "a.different.event.name";

        final JsonSchemaValidationInterceptor eventValidationInterceptor = buildTheClassForTest(
                compiledClass,
                new MyCustomEventFilter(eventName));

        final TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getStringProperty(JMS_HEADER_CPPNAME)).thenReturn(messageName);

        assertThat(eventValidationInterceptor.shouldValidate(textMessage), is(false));
    }

    private JsonSchemaValidationInterceptor buildTheClassForTest(final Class<?> generatedClass, final MyCustomEventFilter myCustomEventFilter) throws Exception {
        final Object myCustomEventFilterInterceptor = generatedClass.newInstance();

        final Field eventFilterField = generatedClass.getDeclaredField("eventFilter");
        eventFilterField.setAccessible(true);

        eventFilterField.set(myCustomEventFilterInterceptor, myCustomEventFilter);

        return (JsonSchemaValidationInterceptor) myCustomEventFilterInterceptor;
    }
}
