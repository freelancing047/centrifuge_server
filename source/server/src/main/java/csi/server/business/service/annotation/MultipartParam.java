/*
 * @(#) MultipartParam.java,  29.04.2010
 *
 */
package csi.server.business.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to bind a service operation's parameter to a request's multi-part Map info query parameter.
 * 
 * @author <a href="mailto:iulian.boanca@lpro.leverpointinc.com">Iulian Boanca</a>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER })
public @interface MultipartParam {

    String value();
}
