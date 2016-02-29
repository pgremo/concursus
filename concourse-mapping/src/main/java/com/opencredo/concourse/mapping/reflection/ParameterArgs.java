package com.opencredo.concourse.mapping.reflection;

import com.opencredo.concourse.data.tuples.TupleKey;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSlot;
import com.opencredo.concourse.mapping.annotations.Name;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ParameterArgs {

    public static ParameterArgs forMethod(Method method, int skip) {
        checkNotNull(method, "method must not be null");
        checkArgument(method.getParameterCount() >= skip,
                "method %s must have at least %s arguments", method, skip);

        Parameter[] parameters = method.getParameters();
        Type[] types = method.getGenericParameterTypes();

        Map<String, Type> typesByName = IntStream.range(skip, method.getParameterCount())
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toMap(
                        i -> getParameterName(parameters[i]),
                        i -> types[i]
                ));

        return new ParameterArgs(typesByName);
    }

    private static String getParameterName(Parameter parameter) {
        return parameter.isAnnotationPresent(Name.class)
                ? parameter.getAnnotation(Name.class).value()
                : parameter.getName();
    }

    private final Map<String, Type> typesByName;

    private ParameterArgs(Map<String, Type> typesByName) {
        this.typesByName = typesByName;
    }

    private <T> Stream<T> streamOverNamesAndTypes(BiFunction<String, Type, T> combiner) {
        return typesByName.entrySet().stream().map(e -> combiner.apply(e.getKey(), e.getValue()));
    }

    private TupleSlot[] getTupleSlots() {
        return streamOverNamesAndTypes(TupleSlot::of)
                .sorted(Comparator.comparing(TupleSlot::getName))
                .toArray(TupleSlot[]::new);
    }

    public TupleSchema getTupleSchema(String name) {
        return TupleSchema.of(name, getTupleSlots());
    }

    public TupleKey[] getTupleKeys(TupleSchema schema) {
        return streamOverNamesAndTypes(schema::getKey).toArray(TupleKey[]::new);
    }
}
