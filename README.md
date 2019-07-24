# modelmapper-spring-boot-starter

[![Build Status](https://travis-ci.org/yoshikawaa/modelmapper-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yoshikawaa/modelmapper-spring-boot-starter)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4a25932a37744e39903c03f749be0726)](https://www.codacy.com/app/yoshikawaa/modelmapper-spring-boot-starter?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yoshikawaa/modelmapper-spring-boot-starter&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4a25932a37744e39903c03f749be0726)](https://www.codacy.com/app/yoshikawaa/modelmapper-spring-boot-starter?utm_source=github.com&utm_medium=referral&utm_content=yoshikawaa/modelmapper-spring-boot-starter&utm_campaign=Badge_Coverage)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.yoshikawaa.modelmapper.spring.boot/modelmapper-spring-boot-starter.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/yoshikawaa/modelmapper/spring/boot/modelmapper-spring-boot-starter/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yoshikawaa.modelmapper.spring.boot/modelmapper-spring-boot-starter.svg)](https://repo.maven.apache.org/maven2/io/github/yoshikawaa/modelmapper/spring/boot/modelmapper-spring-boot-starter/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat)](https://github.com/yoshikawaa/modelmapper-spring-boot-starter/blob/master/LICENSE.txt)

Spring Boot Starter for [ModelMapper](http://modelmapper.org).

## Notes

* Supports upper Java 8
* Supports ModelMapper 2.3.5
* Supports Spring Boot 2.1.6, 2.2.0(experimental)

----

## Getting Start

### Configure dependency

Add dependency `modelmapper-spring-boot-starter`.

```xml
<dependency>
    <groupId>io.github.yoshikawaa.modelmapper.spring.boot</groupId>
    <artifactId>modelmapper-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Use ModelMapper

All you need is define a `@Autowired` field.

```java
@Autowired
private ModelMapper modelMapper;

public Destination sample(Source source) {
    return modelMapper.map(source, Destination.class);
}
```

----

## Features

### Bean Based Configuration

#### Property Mapping

Define a `TypeMapConfigurer` bean.
Please see [Property Mapping](http://modelmapper.org/user-manual/property-mapping/).

```java
@Bean
TypeMapConfigurer<Source, Destination> typeMap() {
    return new TypeMapConfigurer<Source, Destination>() {
        @Override
        public void configure(TypeMap<Source, Destination> typeMap) {
            typeMap.addMapping(Source::getName, Destination::setUsername);
        }
    };
}
```

#### Converters

Define a `Converter` or `AbstractConverter`bean.
Please see [Converters](http://modelmapper.org/user-manual/converters/).

```java
@Bean
Converter<String, String> converter() {
    return new Converter<String, String>() {
        @Override
        public String convert(MappingContext<String, String> context) {
            return context.getSource() == null ? null : context.getSource().toUppercase();
        }
    };
}
```

> Notice.
> Now, `Converter` for the entire application can not be defined as functional interface. See [modelmapper#487](https://github.com/modelmapper/modelmapper/issues/487).

#### Providers

Define a `Provider` or `AbstractProvider`bean.
Please see [Providers](http://modelmapper.org/user-manual/providers/).

```java
@Bean
Provider<Object> provider() {
    return request -> {
        Object source = request.getSource();
        return source != null && request.getRequestedType().isAssignableFrom(source.getClass()) ? source : null;
    };
}
```

> Notice.
> Only one `Provider` can be registered for the entire application.
> Please register specific type `Provider` using Property Mapping.

#### Conditional Mapping

Define a `Condition` bean.
Please see [Conditional Mapping](http://modelmapper.org/user-manual/property-mapping/#conditional-mapping).

```java
@Bean
Condition<String, String> condition() {
    return context -> !StringUtils.isEmpty(context.getSource());
}
```

> Notice.
> Only one `Condition` can be registered for the entire application.
> Please register specific type `Condition` using Property Mapping.

#### More Customize

You can customize `ModelMapper` freely to define a `Module` bean.

```java
@Bean
Module module() {
    return modelMapper -> /* cutomize */;
}
```
### Property Based Configuration

#### Behavior

Change the behavior of `ModelMapper`.
Please see [Configuration](http://modelmapper.org/user-manual/configuration/).

| name                                         | candidates                                       |
|----------------------------------------------|--------------------------------------------------|
| modelmapper.source-name-tokenizer            | `Camel Case`,`Underscore`                        |
| modelmapper.source-name-transformer          | `Javabeans Accessor`,`Javabeans Mutator`         |
| modelmapper.source-naming-convention         | `Javabeans Accessor`,`Javabeans Mutator`,`None`  |
| modelmapper.destination-name-tokenizer       | `Camel Case`,`Underscore`                        |
| modelmapper.destination-name-transforme      | `Javabeans Accessor`,`Javabeans Mutator`         |
| modelmapper.destination-naming-convention    | `Javabeans Accessor`,`Javabeans Mutator`,`None`  |
| modelmapper.matching-strategy                | `Loose`,`Standard`,`Strict`                      |
| modelmapper.field-access-level               | `public`,`protected`,`package_private`,`private` |
| modelmapper.method-access-level              | `public`,`protected`,`package_private`,`private` |
| modelmapper.field-matching-enabled           | boolean                                          |
| modelmapper.ambiguity-ignored                | boolean                                          |
| modelmapper.full-type-matching-required      | boolean                                          |
| modelmapper.implicit-mapping-enabled         | boolean                                          |
| modelmapper.skip-null-enabled                | boolean                                          |
| modelmapper.collections-merge-enabled        | boolean                                          |
| modelmapper.use-o-s-gi-class-loader-bridging | boolean                                          |
| modelmapper.deep-copy-enabled                | boolean                                          |

#### Spring Integration

Register `SpringProvider` use Spring BeanFactory to provide destination objects.
Please see [Spring Integration](http://modelmapper.org/user-manual/spring-integration/).
 
| name                                | candidates |
|-------------------------------------|------------|
| modelmapper.spring-provider-enabled | boolean    |

#### Validation

Validate Property Mapping.
Please see [Validation](http://modelmapper.org/user-manual/validation/).

| name                         | candidates |
|------------------------------|------------|
| modelmapper.validate-enabled | boolean    |

#### Logging

If you want to log Configuration, enable trace level log.

| name                                                                     | candidates |
|--------------------------------------------------------------------------|------------|
| logging.level.io.github.yoshikawaa.modelmapper.spring.boot.autoconfigure | `trace`    |

