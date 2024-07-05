package csi.server.task.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(value= { ElementType.TYPE, ElementType.METHOD } )
public @interface PostInvoke {
    Class<? extends InvokeListener>[] listeners();

}
