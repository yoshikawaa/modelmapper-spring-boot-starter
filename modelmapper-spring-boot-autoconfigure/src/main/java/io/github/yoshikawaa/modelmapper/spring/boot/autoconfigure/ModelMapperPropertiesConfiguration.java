/**
 * Copyright (c) 2019 Atsushi Yoshikawa (https://yoshikawaa.github.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yoshikawaa.modelmapper.spring.boot.autoconfigure;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.modelmapper.spi.MatchingStrategy;
import org.modelmapper.spi.NameTokenizer;
import org.modelmapper.spi.NameTransformer;
import org.modelmapper.spi.NamingConvention;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration Properties Binding Configuration for {@link ModelMapperProperties}.
 *
 * @author Atsushi Yoshikawa
 */
@Slf4j
@Configuration
@ConditionalOnClass(ModelMapper.class)
public class ModelMapperPropertiesConfiguration {

    private static final String LOG_FORMAT_INVALID = "{} not allow [{}], use default.";

    /**
     * Build {@link Converter} from {@link String} to {@link NameTokenizer}.
     *
     * @return {@link Converter}
     */
    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, NameTokenizer> nameTokenizerConverter() {
        return new Converter<String, NameTokenizer>() {
            @Override
            public NameTokenizer convert(String source) {
                if (StringUtils.isEmpty(source))
                    return null;
                if (source.equalsIgnoreCase(NameTokenizers.CAMEL_CASE.toString()))
                    return NameTokenizers.CAMEL_CASE;
                if (source.equalsIgnoreCase(NameTokenizers.UNDERSCORE.toString()))
                    return NameTokenizers.UNDERSCORE;
                log.warn(LOG_FORMAT_INVALID, NameTokenizer.class.getName(), source);
                return null;
            }
        };
    }

    /**
     * Build {@link Converter} from {@link String} to {@link NameTransformer}.
     *
     * @return {@link Converter}
     */
    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, NameTransformer> nameTransformerConverter() {
        return new Converter<String, NameTransformer>() {
            @Override
            public NameTransformer convert(String source) {
                if (StringUtils.isEmpty(source))
                    return null;
                if (source.equalsIgnoreCase(NameTransformers.JAVABEANS_ACCESSOR.toString()))
                    return NameTransformers.JAVABEANS_ACCESSOR;
                if (source.equalsIgnoreCase(NameTransformers.JAVABEANS_MUTATOR.toString()))
                    return NameTransformers.JAVABEANS_MUTATOR;
                log.warn(LOG_FORMAT_INVALID, NameTransformer.class.getName(), source);
                return null;
            }
        };
    }

    /**
     * Build {@link Converter} from {@link String} to {@link NamingConvention}.
     *
     * @return {@link Converter}
     */
    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, NamingConvention> namingConventionConverter() {
        return new Converter<String, NamingConvention>() {
            @Override
            public NamingConvention convert(String source) {
                if (StringUtils.isEmpty(source))
                    return null;
                if (source.equalsIgnoreCase(NamingConventions.JAVABEANS_ACCESSOR.toString()))
                    return NamingConventions.JAVABEANS_ACCESSOR;
                if (source.equalsIgnoreCase(NamingConventions.JAVABEANS_MUTATOR.toString()))
                    return NamingConventions.JAVABEANS_MUTATOR;
                if (source.equalsIgnoreCase(NamingConventions.NONE.toString()))
                    return NamingConventions.NONE;
                log.warn(LOG_FORMAT_INVALID, NamingConvention.class.getName(), source);
                return null;
            }
        };
    }

    /**
     * Build {@link Converter} from {@link String} to {@link MatchingStrategy}.
     *
     * @return {@link Converter}
     */
    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, MatchingStrategy> matchingStrategyConverter() {
        return new Converter<String, MatchingStrategy>() {
            @Override
            public MatchingStrategy convert(String source) {
                if (StringUtils.isEmpty(source))
                    return null;
                if (source.equalsIgnoreCase(MatchingStrategies.LOOSE.toString()))
                    return MatchingStrategies.LOOSE;
                if (source.equalsIgnoreCase(MatchingStrategies.STANDARD.toString()))
                    return MatchingStrategies.STANDARD;
                if (source.equalsIgnoreCase(MatchingStrategies.STRICT.toString()))
                    return MatchingStrategies.STRICT;
                log.warn(LOG_FORMAT_INVALID, MatchingStrategy.class.getName(), source);
                return null;
            }
        };
    }
}
