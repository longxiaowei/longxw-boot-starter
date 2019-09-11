package com.longxw.boot.starter.updater.tool;

import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTool {

    public static <T> Consumer<T> tryLambda(Consumer<T> wrapper){
        return t -> {
            try {
                wrapper.accept(t);
            } catch (Exception ex){
                throw ex;
            }
        };
    }

    public static <T, R> Function<T, R> tryLambda(Function<T, R> wrapper){
        return t -> {
            try {
                return wrapper.apply(t);
            } catch (Exception ex){
                throw ex;
            }
        };
    }

}
