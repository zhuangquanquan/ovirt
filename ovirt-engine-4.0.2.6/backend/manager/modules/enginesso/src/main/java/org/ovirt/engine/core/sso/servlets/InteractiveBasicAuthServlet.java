package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.AuthenticationException;
import org.ovirt.engine.core.sso.utils.Credentials;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveBasicAuthServlet extends HttpServlet {
    private static final long serialVersionUID = -2049151874771762209L;

    private static Logger log = LoggerFactory.getLogger(InteractiveBasicAuthServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Extracting basic auth credentials from header");
        Credentials credentials = SsoUtils.getUserCredentialsFromHeader(request);
        boolean credentialsValid = false;
        try {
            credentialsValid = credentials != null && credentials.isValid();
        } catch (AuthenticationException ex) {
            log.error("Error validating credentials: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
        if (credentialsValid) {
            log.debug("Credentials Valid redirecting to url: {}", SsoConstants.INTERACTIVE_LOGIN_URI);
            SsoUtils.getSsoSession(request).setTempCredentials(credentials);
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_URI);
        } else {
            log.debug("Redirecting to url: {}", SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
            response.sendRedirect(request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI);
        }
    }
}
