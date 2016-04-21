package org.jtwig.translate.integration;

import org.jtwig.i18n.source.message.MapMessageSource;
import org.jtwig.translate.TranslateExtension;
import org.jtwig.translate.configuration.DefaultTranslateConfiguration;
import org.jtwig.translate.configuration.StaticLocaleSupplier;
import org.jtwig.translate.configuration.TranslateConfigurationBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.jtwig.JtwigModel.newModel;
import static org.jtwig.JtwigTemplate.inlineTemplate;
import static org.jtwig.environment.EnvironmentConfigurationBuilder.configuration;

public class TranslateFunctionsTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void translateSimple() throws Exception {
        String result =
                inlineTemplate("{{ 'Hi' | translate }}", configuration()
                        .extensions().add(new TranslateExtension(new DefaultTranslateConfiguration())).and()
                        .build())
                        .render(newModel());

        assertThat(result, is("Hi"));
    }

    @Test
    public void translateWithTranslation() throws Exception {
        Locale current = Locale.ITALIAN;

        String result =
                inlineTemplate("{{ 'Hi' | translate }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                                .messages().withMessageSource(current, singleEntryMap("Hi", "Ciao")).and()
                                .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                                .build())).and()
                                .build()
                ).render(newModel());

        assertThat(result, is("Ciao"));
    }

    @Test
    public void translateWithParameters() throws Exception {
        Locale current = Locale.ITALIAN;

        String result =
                inlineTemplate("{{ 'Hi %name%' | translate({ '%name%': 'Joao' }) }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                                .messages().withMessageSource(current, singleEntryMap("Hi %name%", "Ciao %name%")).and()
                                .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                                .build())).and().build())
                        .render(newModel());

        assertThat(result, is("Ciao Joao"));
    }

    @Test
    public void translateWithInvalidSecondArgument() throws Exception {
        Locale current = Locale.ITALIAN;

        expectedException.expectMessage(containsString("Expecting map or locale as second argument, but got '1'"));

        inlineTemplate("{{ 'Hi %name%' | translate(1) }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                        .messages().withMessageSource(current, singleEntryMap("Hi %name%", "Ciao %name%")).and()
                        .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                        .build())).and().build())
                        .render(newModel());
    }

    @Test
    public void translateWithParametersInAnotherLocale() throws Exception {
        Locale current = Locale.ITALIAN;

        String result =
                inlineTemplate("{{ 'Hi %name%' | translate({ '%name%': 'Joao' }, 'pt') }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                                .messages().withMessageSource(current, singleEntryMap("Hi %name%", "Ciao %name%"))
                                .withMessageSource(Locale.forLanguageTag("pt"), singleEntryMap("Hi %name%", "Ola %name%"))
                                .and()
                                .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                                .build())).and()
                        .build())
        .render(newModel());

        assertThat(result, is("Ola Joao"));
    }

    @Test
    public void translateWithParametersInAnotherLocaleWithWrong2ndParameter() throws Exception {
        Locale current = Locale.ITALIAN;

        expectedException.expectMessage(containsString("Expecting map as second argument, but got '1'"));

        inlineTemplate("{{ 'Hi %name%' | translate(1, 'pt') }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                        .messages().withMessageSource(current, singleEntryMap("Hi %name%", "Ciao %name%"))
                        .withMessageSource(Locale.forLanguageTag("pt"), singleEntryMap("Hi %name%", "Ola %name%"))
                        .and()
                        .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                        .build())).and()
                        .build())
        .render(newModel());
    }

    @Test
    public void translateWithParametersInAnotherLocaleWithWrong3rdParameter() throws Exception {
        Locale current = Locale.ITALIAN;

        expectedException.expectMessage(containsString("Expecting locale as third argument, but got '1'"));

        inlineTemplate("{{ 'Hi %name%' | translate({}, 1) }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                        .messages().withMessageSource(current, singleEntryMap("Hi %name%", "Ciao %name%"))
                        .withMessageSource(Locale.forLanguageTag("pt"), singleEntryMap("Hi %name%", "Ola %name%"))
                        .and()
                        .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                        .build())).and()
                        .build())
        .render(newModel());
    }

    @Test
    public void translateInAnotherLocale() throws Exception {
        Locale current = Locale.ITALIAN;

        String result =
                inlineTemplate("{{ 'Hello' | translate('pt') }}", configuration()
                        .extensions().add(new TranslateExtension(new TranslateConfigurationBuilder(new DefaultTranslateConfiguration())
                                .messages().withMessageSource(current, singleEntryMap("Hello", "Ciao"))
                                .withMessageSource(Locale.forLanguageTag("pt"), singleEntryMap("Hello", "Ola"))
                                .and()
                                .withCurrentLocaleSupplier(new StaticLocaleSupplier(current))
                                .build())).and()
                                .build())
                        .render(newModel());

        assertThat(result, is("Ola"));
    }

    private MapMessageSource singleEntryMap(final String origin, final String replacement) {
        return new MapMessageSource(new HashMap<String, String>() {{
            put(origin, replacement);
        }});
    }
}
