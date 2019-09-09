package com.longxw.boot.starter.updater;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(UpdaterConfiguration.class)
@Documented
public @interface EnableUpdater {
}
