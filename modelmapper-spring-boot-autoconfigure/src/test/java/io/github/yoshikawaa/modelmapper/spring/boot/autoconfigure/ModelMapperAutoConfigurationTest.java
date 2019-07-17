package io.github.yoshikawaa.modelmapper.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;
import org.modelmapper.Provider;
import org.modelmapper.TypeMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.modelmapper.spi.MappingContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;

@RunWith(Enclosed.class)
public class ModelMapperAutoConfigurationTest {

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    public static class DefaultTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // setup
            Source source = new Source();
            source.setId("sample");
            // execute
            Destination destination = modelMapper.map(source, Destination.class);
            // assert
            assertThat(destination).isNotNull().extracting(Destination::getId).isEqualTo("sample");
        }
    }

    @TestConfiguration
    static class TypeMapConfig {
        @Bean
        TypeMapConfigurer<Source, Destination> typeMap() {
            return new TypeMapConfigurer<Source, Destination>() {
                @Override
                public void configure(TypeMap<Source, Destination> typeMap) {
                    typeMap.addMapping(Source::getName, Destination::setUsername);
                    typeMap.addMappings(mapper -> mapper.skip(Destination::setEmail));
                }
            };
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @Import(TypeMapConfig.class)
    public static class TypeMapTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // setup
            Source source = new Source();
            source.setName("sample");
            // execute
            Destination destination = modelMapper.map(source, Destination.class);
            // assert
            assertThat(destination).isNotNull().extracting(Destination::getUsername).isEqualTo("sample");
        }
    }

    @TestConfiguration
    static class ConverterConfig {
        @Bean
        Converter<String, Email> converter() {
            // ModelMapper Bug https://github.com/modelmapper/modelmapper/issues/487
            // @formatter:off
            // return context -> {
            //     String[] email = context.getSource().split("@");
            //     return new Email(email[0], email[1]);
            // };
            // @formatter:on
            return new Converter<String, Email>() {
                @Override
                public Email convert(MappingContext<String, Email> context) {
                    String[] email = context.getSource().split("@");
                    return new Email(email[0], email[1]);
                }
            };
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @Import(ConverterConfig.class)
    public static class ConverterTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // setup
            Source source = new Source();
            source.setEmail("sample@email.com");
            // execute
            Destination destination = modelMapper.map(source, Destination.class);
            // assert
            assertThat(destination).isNotNull().extracting(Destination::getEmail).hasToString("sample@email.com");
        }
    }

    @TestConfiguration
    static class ProviderConfig {
        @Bean
        Provider<Object> provider() {
            return request -> {
                Object source = request.getSource();
                return source != null && request.getRequestedType().isAssignableFrom(source.getClass()) ? source : null;
            };
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @Import(ProviderConfig.class)
    public static class ProviderTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // setup
            Source source = new Source();
            source.setId("sample");
            // execute
            Destination destination = modelMapper.map(source, Destination.class);
            // assert
            assertThat(destination).isNotNull().extracting(Destination::getId).isEqualTo("sample");
        }
    }

    @TestConfiguration
    static class PropertyConditionConfig {
        @Bean
        Condition<String, String> condition() {
            return context -> !StringUtils.isEmpty(context.getSource());
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @Import(PropertyConditionConfig.class)
    public static class PropertyConditionTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // setup
            Source source = new Source();
            source.setId("");
            // execute
            Destination destination = modelMapper.map(source, Destination.class);
            // assert
            assertThat(destination).isNotNull().extracting(Destination::getId).isNull();
        }
    }

    @TestConfiguration
    static class ModuleConfig {
        @Bean
        Module module() {
            return modelMapper -> modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @Import(ModuleConfig.class)
    public static class ModuleTest {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // assert
            assertThat(modelMapper.getConfiguration().getMatchingStrategy()).isEqualTo(MatchingStrategies.STRICT);
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @TestPropertySource("classpath:/test1.properties")
    public static class PropertiesTest1 {

        @Autowired
        private ModelMapper modelMapper;

        @Autowired
        private ModelMapperProperties properties;

        @ClassRule
        public static OutputCapture output = new OutputCapture();

        public PropertiesTest1() {
            ((Logger) LoggerFactory.getLogger(ModelMapperAutoConfiguration.class)).setLevel(Level.TRACE);
            ((Logger) LoggerFactory.getLogger(ModelMapperPropertiesConfiguration.class)).setLevel(Level.TRACE);
        }

        @Test
        public void test() {
            // assert
            Configuration configuration = modelMapper.getConfiguration();
            assertThat(configuration.getSourceNameTokenizer()).isEqualTo(NameTokenizers.CAMEL_CASE);
            assertThat(configuration.getSourceNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_ACCESSOR);
            assertThat(configuration.getSourceNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_ACCESSOR);
            assertThat(configuration.getDestinationNameTokenizer()).isEqualTo(NameTokenizers.CAMEL_CASE);
            assertThat(configuration.getDestinationNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_ACCESSOR);
            assertThat(configuration.getDestinationNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_ACCESSOR);
            assertThat(configuration.getMatchingStrategy()).isEqualTo(MatchingStrategies.LOOSE);
            assertThat(configuration.getFieldAccessLevel()).isEqualTo(AccessLevel.PUBLIC);
            assertThat(configuration.getMethodAccessLevel()).isEqualTo(AccessLevel.PUBLIC);
            assertThat(configuration.isFieldMatchingEnabled()).isEqualTo(true);
            assertThat(configuration.isAmbiguityIgnored()).isEqualTo(true);
            assertThat(configuration.isFullTypeMatchingRequired()).isEqualTo(true);
            assertThat(configuration.isImplicitMappingEnabled()).isEqualTo(true);
            assertThat(configuration.isSkipNullEnabled()).isEqualTo(true);
            // ModelMapper Bug https://github.com/modelmapper/modelmapper/issues/485
            // assertThat(configuration.isCollectionsMergeEnabled()).isEqualTo(true);
            assertThat(configuration.isCollectionsMergeEnabled()).isEqualTo(false);
            assertThat(configuration.isUseOSGiClassLoaderBridging()).isEqualTo(true);
            // ModelMapper Bug https://github.com/modelmapper/modelmapper/issues/485
            // assertThat(configuration.isDeepCopyEnabled()).isEqualTo(true);
            assertThat(configuration.isDeepCopyEnabled()).isEqualTo(false);
            assertThat(properties.isSpringProviderEnabled()).isEqualTo(true);
            assertThat(configuration.getProvider().getClass().getSimpleName()).contains("SpringProvider");
            assertThat(output.toString()).contains("Validate ModelMapper Configuration succeed.");
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @TestPropertySource("classpath:/test2.properties")
    public static class PropertiesTest2 {

        @Autowired
        private ModelMapper modelMapper;

        @ClassRule
        public static OutputCapture output = new OutputCapture();

        public PropertiesTest2() {
            ((Logger) LoggerFactory.getLogger(ModelMapperAutoConfiguration.class)).setLevel(Level.TRACE);
            ((Logger) LoggerFactory.getLogger(ModelMapperPropertiesConfiguration.class)).setLevel(Level.TRACE);
        }

        @Test
        public void test() {
            // assert
            Configuration configuration = modelMapper.getConfiguration();
            assertThat(configuration.getSourceNameTokenizer()).isEqualTo(NameTokenizers.UNDERSCORE);
            assertThat(configuration.getSourceNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_MUTATOR);
            assertThat(configuration.getSourceNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_MUTATOR);
            assertThat(configuration.getDestinationNameTokenizer()).isEqualTo(NameTokenizers.UNDERSCORE);
            assertThat(configuration.getDestinationNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_MUTATOR);
            assertThat(configuration.getDestinationNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_MUTATOR);
            assertThat(configuration.getMatchingStrategy()).isEqualTo(MatchingStrategies.STANDARD);
            assertThat(configuration.getFieldAccessLevel()).isEqualTo(AccessLevel.PROTECTED);
            assertThat(configuration.getMethodAccessLevel()).isEqualTo(AccessLevel.PROTECTED);
            assertThat(configuration.isFieldMatchingEnabled()).isEqualTo(false);
            assertThat(configuration.isAmbiguityIgnored()).isEqualTo(false);
            assertThat(configuration.isFullTypeMatchingRequired()).isEqualTo(false);
            assertThat(configuration.isImplicitMappingEnabled()).isEqualTo(false);
            assertThat(configuration.isSkipNullEnabled()).isEqualTo(false);
            // ModelMapper Bug https://github.com/modelmapper/modelmapper/issues/485
            // assertThat(configuration.isCollectionsMergeEnabled()).isEqualTo(false);
            assertThat(configuration.isCollectionsMergeEnabled()).isEqualTo(true);
            assertThat(configuration.isUseOSGiClassLoaderBridging()).isEqualTo(false);
            // ModelMapper Bug https://github.com/modelmapper/modelmapper/issues/485
            // assertThat(configuration.isDeepCopyEnabled()).isEqualTo(false);
            assertThat(configuration.isDeepCopyEnabled()).isEqualTo(true);
            assertThat(configuration.getProvider()).isNull();
            assertThat(output.toString()).doesNotContain("Validate ModelMapper Configuration succeed.");
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @TestPropertySource("classpath:/test3.properties")
    public static class PropertiesTest3 {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // assert
            Configuration configuration = modelMapper.getConfiguration();
            assertThat(configuration.getSourceNamingConvention()).isEqualTo(NamingConventions.NONE);
            assertThat(configuration.getDestinationNamingConvention()).isEqualTo(NamingConventions.NONE);
            assertThat(configuration.getMatchingStrategy()).isEqualTo(MatchingStrategies.STRICT);
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @TestPropertySource("classpath:/test4.properties")
    public static class PropertiesTest4 {

        @Autowired
        private ModelMapper modelMapper;

        @Test
        public void test() {
            // assert
            Configuration configuration = modelMapper.getConfiguration();
            assertThat(configuration.getSourceNameTokenizer()).isEqualTo(NameTokenizers.CAMEL_CASE);
            assertThat(configuration.getSourceNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_ACCESSOR);
            assertThat(configuration.getSourceNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_ACCESSOR);
            assertThat(configuration.getMatchingStrategy()).isEqualTo(MatchingStrategies.STANDARD);
        }
    }

    @RunWith(SpringRunner.class)
    @ImportAutoConfiguration(ModelMapperAutoConfiguration.class)
    @TestPropertySource("classpath:/test5.properties")
    public static class PropertiesTest5 {

        @Autowired
        private ModelMapper modelMapper;

        @ClassRule
        public static OutputCapture output = new OutputCapture();

        public PropertiesTest5() {
            ((Logger) LoggerFactory.getLogger(ModelMapperAutoConfiguration.class)).setLevel(Level.TRACE);
            ((Logger) LoggerFactory.getLogger(ModelMapperPropertiesConfiguration.class)).setLevel(Level.TRACE);
        }

        @Test
        public void test() {
            // assert
            Configuration configuration = modelMapper.getConfiguration();
            assertThat(configuration.getSourceNameTokenizer()).isEqualTo(NameTokenizers.CAMEL_CASE);
            assertThat(configuration.getSourceNameTransformer()).isEqualTo(NameTransformers.JAVABEANS_ACCESSOR);
            assertThat(configuration.getSourceNamingConvention()).isEqualTo(NamingConventions.JAVABEANS_ACCESSOR);
            assertThat(configuration.getMatchingStrategy()).isEqualTo(MatchingStrategies.STANDARD);
            assertThat(output.toString()).contains(
                    "org.modelmapper.spi.NameTokenizer not allow [invalid], convert to null.",
                    "org.modelmapper.spi.NameTransformer not allow [invalid], convert to null.",
                    "org.modelmapper.spi.NamingConvention not allow [invalid], convert to null.",
                    "org.modelmapper.spi.MatchingStrategy not allow [invalid], convert to null.");
        }
    }

    @Data
    static class Source {
        private String id;
        private String name;
        private String email;
    }

    @Data
    static class Destination {
        private String id;
        private String username;
        private Email email;
    }

    @Data
    @AllArgsConstructor
    static class Email {
        final String prefix;
        final String suffix;

        @Override
        public String toString() {
            return String.join("@", prefix, suffix);
        }
    }
}
