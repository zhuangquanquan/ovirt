package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.Regex;

@SuppressWarnings("unused")
public class RegexValidation implements IValidation {
    protected static final String EMPTY_STRING = "";

    private String privateExpression;

    public String getExpression() {
        return privateExpression;
    }

    public void setExpression(String value) {
        privateExpression = value;
    }

    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private boolean privateIsNegate;

    public boolean getIsNegate() {
        return privateIsNegate;
    }

    public void setIsNegate(boolean value) {
        privateIsNegate = value;
    }

    public RegexValidation() {
    }

    public RegexValidation(String expression, String message) {
        setExpression(expression);
        setMessage(message);
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value == null) {
            value = EMPTY_STRING;
        }

        if (value instanceof String) {
            final String stringValue = (String) value;
            if (getIsNegate() ^ !Regex.isMatch(stringValue, getExpression())) {
                result.setSuccess(false);
                result.getReasons().add(getMessage());
            }
        }

        return result;
    }

    protected String start() {
        return "^"; //$NON-NLS-1$
    }

    protected String end() {
        return "$"; //$NON-NLS-1$
    }
}
