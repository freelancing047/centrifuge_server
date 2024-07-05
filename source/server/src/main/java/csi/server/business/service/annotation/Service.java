/*
 * @(#) Service.java,  02.04.2010
 *
 */
package csi.server.business.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Annotation for a Service Class that is allowed to be loaded at runtime.
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    String path() default "";
}
