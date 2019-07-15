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

import java.util.List;
import java.util.Optional;

import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;
import org.modelmapper.Provider;
import org.modelmapper.config.Configuration;
import org.modelmapper.spring.SpringIntegration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot Auto Configuration for {@link ModelMapper}.
 *
 * @author Atsushi Yoshikawa
 */
@Slf4j
@org.springframework.context.annotation.Configuration
@ConditionalOnClass(ModelMapper.class)
@EnableConfigurationProperties(ModelMapperProperties.class)
@Import(ModelMapperPropertiesConfiguration.class)
public class ModelMapperAutoConfiguration {

    /**
     * Build {@link Provider} for Spring Integration.
     *
     * @param beanFactory Spring Bean Factory
     * @return Spring Provider
     */
    @Bean
    @ConditionalOnProperty(prefix = ModelMapperProperties.PROPERTIES_PREFIX, name = ModelMapperProperties.PROPERTIES_SPRING_PROVIDER_ENABLED)
    public Provider<?> springProvider(BeanFactory beanFactory) {
        return SpringIntegration.fromSpring(beanFactory);
    }

    /**
     * Build {@link ModelMapper}.
     *
     * @param properties                 Properties for {@link ModelMapper}
     * @param providerProvider           {@link Provider} bean
     * @param conditionProvider          {@link Condition} bean
     * @param typeMapConfigurersProvider {@link TypeMapConfigurer} beans
     * @param convertersProvider         {@link Converter} beans
     * @param modulesProvider            {@link Module} beans
     * @return Configured {@link ModelMapper}
     */
    @Bean
    @ConditionalOnMissingBean(ModelMapper.class)
    public ModelMapper modelMapper(ModelMapperProperties properties,
            ObjectProvider<Provider<?>> providerProvider,
            ObjectProvider<Condition<?, ?>> conditionProvider,
            ObjectProvider<List<TypeMapConfigurer<?, ?>>> typeMapConfigurersProvider,
            ObjectProvider<List<Converter<?, ?>>> convertersProvider,
            ObjectProvider<List<Module>> modulesProvider) {

        log.trace("Configure ModelMapper with ModelMapperAutoConfiguration.");
        ModelMapper modelMapper = new ModelMapper();

        configureProperties(modelMapper.getConfiguration(), properties);
        configureMappings(modelMapper, providerProvider, conditionProvider, typeMapConfigurersProvider,
                convertersProvider, modulesProvider);
        validateMappings(modelMapper, properties);
        loggingConfiguration(modelMapper);

        return modelMapper;
    }

    private void configureProperties(Configuration configuration, ModelMapperProperties properties) {

        Optional.ofNullable(properties.getSourceNameTokenizer())
                .ifPresent(nameTokenizer -> configuration.setSourceNameTokenizer(nameTokenizer));
        Optional.ofNullable(properties.getSourceNameTransformer())
                .ifPresent(nameTransformer -> configuration.setSourceNameTransformer(nameTransformer));
        Optional.ofNullable(properties.getSourceNamingConvention())
                .ifPresent(namingConvention -> configuration.setSourceNamingConvention(namingConvention));
        Optional.ofNullable(properties.getDestinationNameTokenizer())
                .ifPresent(nameTokenizer -> configuration.setDestinationNameTokenizer(nameTokenizer));
        Optional.ofNullable(properties.getDestinationNameTransformer())
                .ifPresent(nameTransformer -> configuration.setDestinationNameTransformer(nameTransformer));
        Optional.ofNullable(properties.getDestinationNamingConvention())
                .ifPresent(namingConvention -> configuration.setDestinationNamingConvention(namingConvention));
        Optional.ofNullable(properties.getMatchingStrategy())
                .ifPresent(matchingStrategy -> configuration.setMatchingStrategy(matchingStrategy));
        Optional.ofNullable(properties.getFieldAccessLevel())
                .ifPresent(accessLevel -> configuration.setFieldAccessLevel(accessLevel));
        Optional.ofNullable(properties.getMethodAccessLevel())
                .ifPresent(accessLevel -> configuration.setMethodAccessLevel(accessLevel));
        Optional.ofNullable(properties.getFieldMatchingEnabled())
                .ifPresent(enabled -> configuration.setFieldMatchingEnabled(enabled));
        Optional.ofNullable(properties.getAmbiguityIgnored())
                .ifPresent(ignore -> configuration.setAmbiguityIgnored(ignore));
        Optional.ofNullable(properties.getFullTypeMatchingRequired())
                .ifPresent(required -> configuration.setFullTypeMatchingRequired(required));
        Optional.ofNullable(properties.getSkipNullEnabled())
                .ifPresent(enabled -> configuration.setSkipNullEnabled(enabled));
        Optional.ofNullable(properties.getImplicitMappingEnabled())
                .ifPresent(enabled -> configuration.setImplicitMappingEnabled(enabled));
        Optional.ofNullable(properties.getCollectionsMergeEnabled())
                .ifPresent(enabled -> configuration.setCollectionsMergeEnabled(enabled));
        Optional.ofNullable(properties.getUseOSGiClassLoaderBridging())
                .ifPresent(useOSGiClassLoaderBridging -> configuration.setUseOSGiClassLoaderBridging(useOSGiClassLoaderBridging));
        Optional.ofNullable(properties.getDeepCopyEnabled())
                .ifPresent(enabled -> configuration.setDeepCopyEnabled(enabled));
    }

    private void configureMappings(ModelMapper modelMapper,
            ObjectProvider<Provider<?>> providerProvider,
            ObjectProvider<Condition<?, ?>> conditionProvider,
            ObjectProvider<List<TypeMapConfigurer<?, ?>>> typeMapConfigurersProvider,
            ObjectProvider<List<Converter<?, ?>>> convertersProvider,
            ObjectProvider<List<Module>> modulesProvider) {

        Configuration configuration = modelMapper.getConfiguration();

        providerProvider.ifAvailable(provider -> configuration.setProvider(provider));
        conditionProvider.ifAvailable(condition -> configuration.setPropertyCondition(condition));
        typeMapConfigurersProvider.ifAvailable(typeMapConfigurers -> typeMapConfigurers
                .forEach(typeMapConfigurer -> typeMapConfigurer.typeMap(modelMapper)));
        convertersProvider
                .ifAvailable(converters -> converters.forEach(converter -> modelMapper.addConverter(converter)));
        modulesProvider.ifAvailable(modules -> modules.forEach(module -> modelMapper.registerModule(module)));
    }

    private void validateMappings(ModelMapper modelMapper, ModelMapperProperties properties) {

        if (properties.isValidateEnabled()) {
            modelMapper.validate();
            log.trace("Validate ModelMapper Configuration succeed.");
        }
    }

    private void loggingConfiguration(ModelMapper modelMapper) {

        if (log.isTraceEnabled()) {
            log.trace(
                    "ModelMapper Configuration ==========================================================================");

            Configuration configuration = modelMapper.getConfiguration();
            log.trace(" SourceNameTokenizer : {}", configuration.getSourceNameTokenizer());
            log.trace(" SourceNameTransformer : {}", configuration.getSourceNameTransformer());
            log.trace(" SourceNamingConvention : {}", configuration.getSourceNamingConvention());
            log.trace(" DestinationNameTokenizer : {}", configuration.getDestinationNameTokenizer());
            log.trace(" DestinationNameTransformer : {}", configuration.getDestinationNameTransformer());
            log.trace(" DestinationNameTransformer : {}", configuration.getDestinationNameTransformer());
            log.trace(" DestinationNamingConvention : {}", configuration.getDestinationNamingConvention());
            log.trace(" MatchingStrategy : {}", configuration.getMatchingStrategy());
            log.trace(" FieldAccessLevel : {}", configuration.getFieldAccessLevel());
            log.trace(" MethodAccessLevel : {}", configuration.getMethodAccessLevel());
            log.trace(" FieldMatchingEnabled : {}", configuration.isFieldMatchingEnabled());
            log.trace(" AmbiguityIgnored : {}", configuration.isAmbiguityIgnored());
            log.trace(" FullTypeMatchingRequired : {}", configuration.isFullTypeMatchingRequired());
            log.trace(" ImplicitMappingEnabled : {}", configuration.isImplicitMappingEnabled());
            log.trace(" SkipNullEnabled : {}", configuration.isSkipNullEnabled());
            log.trace(" CollectionsMergeEnabled : {}", configuration.isCollectionsMergeEnabled());
            log.trace(" UseOSGiClassLoaderBridging : {}", configuration.isUseOSGiClassLoaderBridging());
            log.trace(" DeepCopyEnabled : {}", configuration.isDeepCopyEnabled());
            log.trace(" Provider : {}", configuration.getProvider());
            log.trace(" PropertyCondition : {}", configuration.getPropertyCondition());
            log.trace(" TypeMaps :");
            modelMapper.getTypeMaps().forEach(typeMap -> log.trace("  {}", typeMap));
            log.trace(" Converters :");
            configuration.getConverters().forEach(converter -> log.trace("  {}", converter));

            log.trace(
                    "====================================================================================================");
        }
    }
}
