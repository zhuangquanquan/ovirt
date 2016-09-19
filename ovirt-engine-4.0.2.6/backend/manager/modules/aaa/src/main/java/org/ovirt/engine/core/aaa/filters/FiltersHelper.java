package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicHeaderValueParser;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiltersHelper {

    public static final String VERSION = "0";
    private static final Logger log = LoggerFactory.getLogger(FiltersHelper.class);
    private static SecureRandom secureRandom;
    public static class Constants {
        public static final String REQUEST_AUTH_RECORD_KEY = "ovirt_aaa_auth_record";
        public static final String REQUEST_SCHEMES_KEY = "ovirt_aaa_schemes";
        public static final String REQUEST_PROFILE_KEY = "ovirt_aaa_profile";
        public static final String REQUEST_AUTH_TYPE_KEY = "ovirt_aaa_auth_type";
        public static final String REQUEST_PASSWORD_KEY = "ovirt_aaa_password";
        public static final String REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE =
                "ovirt_aaa_login_filter_authentication_done";
        public static final String HEADER_AUTHORIZATION = "Authorization";
        public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
        public static final String HEADER_PREFER = "Prefer";
        public static final String LOGOUT_INPROGRESS = "LOGOUT_INPROGRESS";
    }

    public static final int PREFER_NEW_AUTH = 1<<0;
    public static final int PREFER_PERSISTENCE_AUTH = 1<<1;

    public static BackendLocal getBackend(Context context) {

        try {
            return (BackendLocal) context.lookup("java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return (request.getSession(false) != null && request.getSession(false)
                .getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY) != null)
                || request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY) != null;
    }

    public static boolean isStatusOk(Map<String, Object> response) {
        if (response.get("error") != null) {
            throw new RuntimeException(String.format("%s: %s", response.get("error_code"), response.get("error")));
        }
        return true;
    }

    public static boolean isSessionValid(HttpServletRequest req) {
        String engineSessionId = (String) req.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        if (StringUtils.isEmpty(engineSessionId)) {
            engineSessionId = (String) req.getSession(true).getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        }
        return isSessionValid(engineSessionId);
    }

    public static boolean isSessionValid(String session) {
        try {
            InitialContext ctx = null;
            try {
                ctx = new InitialContext();
                VdcQueryReturnValue returnValue =
                        FiltersHelper.getBackend(ctx)
                                .runPublicQuery(VdcQueryType.ValidateSession,
                                        new VdcQueryParametersBase(session));
                return returnValue.getSucceeded();
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String generateState() throws NoSuchAlgorithmException {
        if (secureRandom == null) {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }

        byte[] s = new byte[8];
        secureRandom.nextBytes(s);
        return new Base64(0, new byte[0], true).encodeToString(s);
    }

    public static String getRedirectUriServerName(String name) {
        return InetAddressUtils.isIPv6Address(name) ? String.format("[%s]", name) : name;
    }

    public static Map<String, Object> getPayloadForAuthCode(
            String authCode,
            String scope,
            String redirectUri) throws Exception {
        Map<String, Object> response = SsoOAuthServiceUtils.getToken("authorization_code", authCode, scope, redirectUri);
        FiltersHelper.isStatusOk(response);
        return getPayloadForToken((String) response.get("access_token"));
    }

    public static Map<String, Object> getPayloadForToken(String token) throws Exception {
        Map<String, Object> response = SsoOAuthServiceUtils.getTokenInfo(token);
        FiltersHelper.isStatusOk(response);
        response.put(SessionConstants.SSO_TOKEN_KEY, token);
        return response;
    }

    public static HttpURLConnection create(URL url) throws IOException, GeneralSecurityException {
        return new HttpURLConnectionBuilder(url).setHttpsProtocol("TLSv1")
                .setReadTimeout(0)
                .setTrustManagerAlgorithm(TrustManagerFactory.getDefaultAlgorithm())
                .setTrustStore(EngineLocalConfig.getInstance().getProperty("ENGINE_PKI_TRUST_STORE"))
                .setTrustStorePassword(EngineLocalConfig.getInstance().getPKITrustStorePassword())
                .setTrustStoreType(KeyStore.getDefaultType())
                .setURL(url)
                .setVerifyChain(true)
                .setVerifyHost(false).create();
    }

    public static long copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[8*1024];
        long count = 0;
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static int getPrefer(HttpServletRequest req) {
        int ret = 0;
        Enumeration<String> headerValues = req.getHeaders(Constants.HEADER_PREFER);
        while (headerValues.hasMoreElements()) {
            String headerValue = headerValues.nextElement();
            HeaderElement[] headerElements = BasicHeaderValueParser.parseElements(headerValue, null);
            if (headerElements != null) {
                for (HeaderElement headerElement : headerElements) {
                    String elementName = headerElement.getName();
                    if ("new-auth".equalsIgnoreCase(elementName)) {
                        ret |= PREFER_NEW_AUTH;
                    }
                    if ("persistent-auth".equalsIgnoreCase(elementName)) {
                        ret |= PREFER_PERSISTENCE_AUTH;
                    }
                }
            }
        }
        return ret;
    }

    public static String getTokenContent(String token) {
        String[] s = token.split("\\|", 2);
        if (s.length != 2) {
            throw new IllegalArgumentException("Invalid session token format");
        }
        if (!"0".equals(s[0])) {
            throw new IllegalArgumentException("Invalid session token version");
        }
        if (s[1].isEmpty()) {
            throw new IllegalArgumentException("Invalid session token format");
        }
        return s[1];
    }
}
