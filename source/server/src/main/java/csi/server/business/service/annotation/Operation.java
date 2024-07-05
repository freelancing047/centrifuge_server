/*
 * @(#) Operation.java,  06.04.2010
 *
 */
package csi.server.business.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Annotation for a Service Method that is allowed to be executed at runtime.
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Operation {

}
