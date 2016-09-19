package org.ovirt.engine.core.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class ValidationUtils {

    public static final String NO_SPECIAL_CHARACTERS_EXTRA_I18N = "^[\\p{L}0-9._\\+-]*$";
    public static final String NO_SPECIAL_CHARACTERS_I18N = "^[\\p{L}0-9._-]*$";
    public static final String NO_SPECIAL_CHARACTERS = "[0-9a-zA-Z_-]+";
    public static final String ONLY_I18N_ASCII_OR_NONE = "[\\p{ASCII}\\p{L}]*";
    public static final String ONLY_ASCII_OR_NONE = "[\\p{ASCII}]*";
    public static final String NO_SPECIAL_CHARACTERS_WITH_DOT = "[0-9a-zA-Z-_\\.]+";
    public static final String NO_TRIMMING_WHITE_SPACES_PATTERN = "^$|\\S.*\\S";
    public static final String IPV4_PATTERN_NON_EMPTY =
            "\\b((25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)";
    public static final String IPV4_PATTERN = "^" + IPV4_PATTERN_NON_EMPTY + "$|^$";
    private static final String IPV6_ADDRESS_BLOCK = "[0-9a-fA-F]{1,4}";
    private static final String IPV6_HEX_COMPRESSED_PATTERN =
            "((?:" + IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)::((?:" +
                    IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)";
    private static final String IPV6_STD_PATTERN = "(?:" + IPV6_ADDRESS_BLOCK + ":){7}" + IPV6_ADDRESS_BLOCK;
    public static final String IPV6_PATTERN = "(?:" + IPV6_STD_PATTERN + "|" + IPV6_HEX_COMPRESSED_PATTERN + ")";
    public static final String IPV6_FOR_URI = "\\[" + IPV6_PATTERN + "\\]";
    public static final String FQDN_PATTERN =
            "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*";
    public static final String HOSTNAME_FOR_URI =
            "(?:" + FQDN_PATTERN + "|" + IPV4_PATTERN + "|" + IPV6_FOR_URI + ")";

    public static final String SUBNET_PREFIX_PATTERN = "(?:3[0-2]|[12]?[0-9])";
    public static final String CIDR_FORMAT_PATTERN = "^" + IPV4_PATTERN_NON_EMPTY + "/" + SUBNET_PREFIX_PATTERN + "$";
    public static final String ISO_SUFFIX = ".iso";
    public static final String ISO_SUFFIX_PATTERN = "^$|^.+\\.iso$";
    public static final String BASE_64_PATTERN =
            "^([A-Za-z0-9+/]{4})*(()|[A-Za-z0-9+/][AQgw]==|[A-Za-z0-9+/]{2}[AEIMQUYcgkosw048]=)$";
    public static final String KEY_EQUALS_VALUE_SPACE_SEPARATED = "^[^\\s=]+=[^\\s=]+(\\s+[^\\s=]+=[^\\s=]+)*$";
    public static final String EMPTY_STRING = "^$";

    /**
     * the mask will be replaced with zero-padded number in the generated names of the VMs in the pool, see
     * NameForVmInPoolGeneratorTest PoolNameValidationTest for valid and invalid expressions of this pattern
     */
    public static final String POOL_NAME_PATTERN = "^[\\p{L}0-9._-]+[" + VmPool.MASK_CHARACTER
            + "]*[\\p{L}0-9._-]*$|^[\\p{L}0-9._-]*[" + VmPool.MASK_CHARACTER + "]*[\\p{L}0-9._-]+$";

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /***
     * This function validates a hostname according to URI RFC's.
     */
    public static boolean validHostname(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return isValidIpAddressOrHostname(address) || isValidIpv6Address(address);
    }

    private static boolean isValidIpAddressOrHostname(String address) {
        try {
            URI uri = new URI("http://" + address);
            return address.equals(uri.getHost());
        } catch (URISyntaxException use) {
            return false;
        }
    }

    private static boolean isValidIpv6Address(String address) {
        final String quotedIpv6 = "[" + address + "]";
        return isValidIpAddressOrHostname(quotedIpv6);
    }

    public static boolean validUri(String addr) {
        try {
            new URI(addr);
            return true;
        } catch (URISyntaxException use) {
            return false;
        }
    }

    public static Validator getValidator() {
        return validator;

    }

    /**
     * @return A list of error message keys representing the violations, or empty list if no violations occurred.
     */
    public static <T> List<String> validateInputs(List<Class<?>> validationGroupList, T parameters) {

        List<String> messages = Collections.emptyList();
        Set<ConstraintViolation<T>> violations = ValidationUtils.getValidator().validate(parameters,
                validationGroupList.toArray(new Class<?>[validationGroupList.size()]));

        if (!violations.isEmpty()) {
            messages = new ArrayList<>(violations.size());

            for (ConstraintViolation<T> constraintViolation : violations) {
                messages.add(constraintViolation.getMessage());
            }
        }
        return messages;
    }

    public static boolean validatePort(int port) {
        return (port >= BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT) && (port <= BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT);
    }
}
