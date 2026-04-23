package uk.gov.justice.raml.jms.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventFilterFieldCodeGeneratorTest {

    @InjectMocks
    private EventFilterFieldCodeGenerator eventFilterFieldCodeGenerator;

    @Test
    public void shouldCreateAFieldForACustomEventFilter() throws Exception {

        final ClassName customEventFilterClassName = ClassName.get("org.acme", "MyCustomEventFilter");

        final FieldSpec eventFilterField = eventFilterFieldCodeGenerator.createEventFilterField(customEventFilterClassName);

        assertThat(eventFilterField.toString(), is("@jakarta.inject.Inject\nprivate org.acme.MyCustomEventFilter eventFilter;\n"));
    }
}
