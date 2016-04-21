package org.jtwig.translate.function.extract;

import com.google.common.base.Optional;
import org.jtwig.exceptions.CalculationException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.i18n.decorate.ReplacementMessageDecorator;
import org.jtwig.translate.configuration.TranslateConfiguration;
import org.jtwig.util.ErrorMessageFormatter;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class TranslateParameterExtractor {
    private final LocaleExtractor localeExtractor;
    private final ReplacementsExtractor replacementsExtractor;
    private final LocaleOrReplacementsExtractor localeOrReplacementsExtractor;

    public TranslateParameterExtractor(LocaleExtractor localeExtractor, ReplacementsExtractor replacementsExtractor, LocaleOrReplacementsExtractor localeOrReplacementsExtractor) {
        this.localeExtractor = localeExtractor;
        this.replacementsExtractor = replacementsExtractor;
        this.localeOrReplacementsExtractor = localeOrReplacementsExtractor;
    }

    public TranslateChoiceParameters extractChoiceForTwoArguments(FunctionRequest request) {
        return new TranslateChoiceParameters(
                TranslateConfiguration.currentLocaleSupplier(request.getEnvironment()).get(),
                Collections.<ReplacementMessageDecorator.Replacement>emptyList()
        );
    }

    public TranslateChoiceParameters extractForThreeArguments (FunctionRequest request) {
        LocaleOrReplacementsExtractor.Result result = localeOrReplacementsExtractor.extractor(request.getEnvironment(), request.get(2));
        if (result.isEmpty()) {
            throw new CalculationException(ErrorMessageFormatter.errorMessage(request.getPosition(), String.format("Expecting map or locale as third argument, but got '%s'", request.get(2))));
        } else {
            Locale locale = result.getLocale().or(TranslateConfiguration.currentLocaleSupplier(request.getEnvironment()));
            Collection<ReplacementMessageDecorator.Replacement> replacements =
                    result.getReplacements().or(Collections.<ReplacementMessageDecorator.Replacement>emptyList());

            return new TranslateChoiceParameters(locale, replacements);
        }
    }

    public TranslateChoiceParameters extractForFourArguments (FunctionRequest request) {
        Collection<ReplacementMessageDecorator.Replacement> replacements;
        Locale locale;

        Optional<Collection<ReplacementMessageDecorator.Replacement>> collectionOptional = replacementsExtractor.extract(request.getEnvironment(), request.get(2));
        if (collectionOptional.isPresent()) {
            replacements = collectionOptional.get();
        } else {
            throw new CalculationException(ErrorMessageFormatter.errorMessage(request.getPosition(), String.format("Expecting map as third argument, but got '%s'", request.get(2))));
        }

        Optional<Locale> localeExtract = localeExtractor.extract(request.getEnvironment(), request.get(3));
        if (localeExtract.isPresent()) {
            locale = localeExtract.get();
        } else {
            throw new CalculationException(ErrorMessageFormatter.errorMessage(request.getPosition(), String.format("Expecting locale as fourth argument, but got '%s'", request.get(3))));
        }

        return new TranslateChoiceParameters(locale, replacements);
    }

    public static class TranslateChoiceParameters {
        private final Locale locale;
        private final Collection<ReplacementMessageDecorator.Replacement> replacements;

        public TranslateChoiceParameters(Locale locale, Collection<ReplacementMessageDecorator.Replacement> replacements) {
            this.locale = locale;
            this.replacements = replacements;
        }

        public Locale getLocale() {
            return locale;
        }

        public Collection<ReplacementMessageDecorator.Replacement> getReplacements() {
            return replacements;
        }
    }
}
