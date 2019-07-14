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
import org.modelmapper.TypeMap;
import org.modelmapper.internal.typetools.TypeResolver;
import org.modelmapper.internal.util.Assert;

/**
 * Configurer for {@link TypeMap}.
 *
 * @author Atsushi Yoshikawa
 *
 * @param <S> source type
 * @param <D> destination type
 */
public abstract class TypeMapConfigurer<S, D> {

    /**
     * Create or get {@link TypeMap} for generic types.
     *
     * @param modelMapper configuring {@link ModelMapper}
     */
    @SuppressWarnings("unchecked")
    public void typeMap(ModelMapper modelMapper) {
        Class<?>[] typeArguments = TypeResolver.resolveRawArguments(TypeMapConfigurer.class, getClass());
        Assert.notNull(typeArguments,
                "Must declare source type argument <S> and destination type argument <D> for TypeMap");
        configure(modelMapper.typeMap((Class<S>) typeArguments[0], (Class<D>) typeArguments[1]));
    }

    /**
     * Configure {@link TypeMap}.
     *
     * @param typeMap configuring {@link TypeMap}
     */
    public abstract void configure(TypeMap<S, D> typeMap);
}
