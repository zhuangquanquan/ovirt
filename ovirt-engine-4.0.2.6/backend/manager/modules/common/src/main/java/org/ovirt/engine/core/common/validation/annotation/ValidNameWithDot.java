package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.utils.ValidationUtils;

@Target({ ANNOTATION_TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_WITH_DOT)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
public @interface ValidNameWithDot {
    String message() default "VALIDATION_NAME_INVALID_WITH_DOT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}