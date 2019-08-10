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
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.spi.MatchingStrategy;
import org.modelmapper.spi.NameTokenizer;
import org.modelmapper.spi.NameTransformer;
import org.modelmapper.spi.NamingConvention;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration Properties for {@link ModelMapper}.
 *
 * @author Atsushi Yoshikawa
 */
@Getter
@Setter
@ConfigurationProperties(prefix = ModelMapperProperties.PROPERTIES_PREFIX)
public class ModelMapperProperties {

    public static final String PROPERTIES_PREFIX = "modelmapper";
    public static final String PROPERTIES_SPRING_PROVIDER_ENABLED = "spring-provider-enabled";

    private Configuration configuration;
    private boolean springProviderEnabled = false;
    private boolean validateEnabled = false;

    /**
     * Configuration Properties for {@link org.modelmapper.config.Configuration}.
     *
     * @author Atsushi Yoshikawa
     */
    @Getter
    @Setter
    public static class Configuration {

        private NameTokenizer sourceNameTokenizer;
        private NameTransformer sourceNameTransformer;
        private NamingConvention sourceNamingConvention;
        private NameTokenizer destinationNameTokenizer;
        private NameTransformer destinationNameTransformer;
        private NamingConvention destinationNamingConvention;
        private MatchingStrategy matchingStrategy;
        private AccessLevel fieldAccessLevel;
        private AccessLevel methodAccessLevel;
        private Boolean fieldMatchingEnabled;
        private Boolean ambiguityIgnored;
        private Boolean fullTypeMatchingRequired;
        private Boolean implicitMappingEnabled;
        private Boolean skipNullEnabled;
        private Boolean collectionsMergeEnabled;
        private Boolean useOSGiClassLoaderBridging;
        private Boolean deepCopyEnabled;
    }
}
