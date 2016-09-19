package org.ovirt.engine.core.sso.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.sso.utils.AuthenticationUtils;
import org.ovirt.engine.core.sso.utils.JsonExtMapMixIn;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoContext;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DirectorySearch {
    GetAvailableNameSpaces(SsoConstants.AVAILABLE_NAMESPACES_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return AuthenticationUtils.getAvailableNamesSpaces(ssoContext.getSsoExtensionsManager());
        }
    },

    GetDomainList(SsoConstants.DOMAIN_LIST_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return ssoContext.getSsoExtensionsManager().getExtensionsByService(
                    Authz.class.getName()).stream()
                    .map(AuthzUtils::getName)
                    .collect(Collectors.toList());
        }
    },

    FetchPrincipalRecord(SsoConstants.FETCH_PRINCIPAL_RECORD_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            Map<String, Object> params = readParams(request);
            return Arrays.asList(AuthzUtils.fetchPrincipalRecord(
                    ssoContext.getSsoExtensionsManager().getExtensionByName(
                            (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN)),
                    (String) params.get(SsoConstants.HTTP_PARAM_PRINCIPAL),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING_RECURSIVE)));
        }
    },

    FindPrincipalById(SsoConstants.FIND_PRINCIPAL_BY_ID_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return getPrincipalById(ssoContext, readParams(request));
        }
    },

    FindPrincipalsByIds(SsoConstants.FIND_PRINCIPALS_BY_IDS_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return getPrincipalsByIds(ssoContext, readParams(request));
        }
    },

    FindLoginOnBehalfPrincipalById(SsoConstants.FIND_LOGIN_ON_BEHALF_PRINCIPAL_BY_ID_QUERY, true) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return getPrincipalsByIds(ssoContext, readParams(request));
        }
    },

    FindDirectoryGroupById(SsoConstants.FIND_DIRECTORY_GROUP_BY_ID_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return getDirectoryGroupById(ssoContext, readParams(request));
        }
    },

    ProfileList(SsoConstants.PROFILE_LIST_QUERY, true) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return getProfileList(ssoContext);
        }
    },

    SearchUsers(SsoConstants.SEARCH_USERS_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return searchDirectoryUsers(ssoContext, readParams(request));
        }
    },

    SearchGroups(SsoConstants.SEARCH_GROUPS_QUERY, false) {
        public Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception {
            return searchDirectoryGroups(ssoContext, readParams(request));
        }
    };

    private static Map<String, Object> readParams(HttpServletRequest request) throws Exception {
        return initMapper().readValue(
                SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_PARAMS),
                HashMap.class);
    }

    private static ObjectMapper initMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        return mapper;
    }

    public static final Logger log = LoggerFactory.getLogger(DirectorySearch.class);
    private String name;
    private boolean isPublicQuery;

    DirectorySearch(String name, boolean isPublicQuery) {
        this.name = name;
        this.isPublicQuery = isPublicQuery;
    }

    public Object executeQuery(SsoContext ssoContext, HttpServletRequest request) throws Exception {
        return execute(ssoContext, request);
    }

    public abstract Object execute(SsoContext ssoContext, HttpServletRequest request) throws Exception;

    public String getName() {
        return name;
    }

    public boolean isPublicQuery() {
        return isPublicQuery;
    }

    private static List<ExtMap> searchDirectoryGroups(SsoContext ssoContext, Map<String, Object> params) {
        log.debug("Entered searchDirectoryGroups");
        String authzName = (String) params.get("authz");
        String query = (String) params.get("query");
        ExtensionProxy authz = AuthenticationUtils.getExtensionProfileByAuthzName(ssoContext, authzName).getAuthz();

        List<ExtMap> results = new ArrayList<>();
        getNamespaces(ssoContext, (String) params.get("namespace"), authzName).
                forEach((namespace) -> results.addAll(DirectoryUtils.findDirectoryGroupsByQuery(authz,
                        namespace,
                        query)));
        log.debug("DirectoryUtils.findDirectoryGroupsByQuery returned {} groups in authz {} for query {}",
                results.size(),
                authzName,
                query);
        return results;
    }

    private static List<ExtMap> getPrincipalById(SsoContext ssoContext, Map<String, Object> params) {
        List<ExtMap> users = new ArrayList<>();
        final ExtensionProxy extension = ssoContext.getSsoExtensionsManager().getExtensionByName(
                (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN));
        String searchNamespace = (String) params.get(SsoConstants.HTTP_PARAM_NAMESPACE);
        for (String namespace : StringUtils.isEmpty(searchNamespace)
                ? getNamespaces(ssoContext, (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN))
                : Arrays.asList(searchNamespace)) {
            users.addAll(AuthzUtils.findPrincipalsByIds(
                    extension,
                    namespace,
                    Arrays.asList((String) params.get(SsoConstants.HTTP_PARAM_ID)),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING_RECURSIVE)));
        }
        return users;
    }

    private static List<ExtMap> getDirectoryGroupById(SsoContext ssoContext, Map<String, Object> params) {
        List<ExtMap> groups = new ArrayList<>();
        final ExtensionProxy extension = ssoContext.getSsoExtensionsManager().getExtensionByName(
                (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN));
        String searchNamespace = (String) params.get(SsoConstants.HTTP_PARAM_NAMESPACE);
        for (String namespace : StringUtils.isEmpty(searchNamespace)
                ? getNamespaces(ssoContext, (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN))
                : Arrays.asList(searchNamespace)) {
            groups.addAll(AuthzUtils.findGroupRecordsByIds(
                    extension,
                    namespace,
                    Arrays.asList((String) params.get(SsoConstants.HTTP_PARAM_ID)),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING),
                    (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING_RECURSIVE)));
        }
        return groups;
    }

    private static List<ExtMap> getPrincipalsByIds(SsoContext ssoContext, Map<String, Object> params) {
        final ExtensionProxy extension = ssoContext.getSsoExtensionsManager().getExtensionByName(
                (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN));
        List<ExtMap> results = new ArrayList<>();
        getNamespaces(ssoContext, (String) params.get(SsoConstants.HTTP_PARAM_NAMESPACE),
                (String) params.get(SsoConstants.HTTP_PARAM_DOMAIN)).
                forEach((namespace) -> results.addAll(AuthzUtils.findPrincipalsByIds(
                        extension,
                        namespace,
                        (Collection<String>) params.get(SsoConstants.HTTP_PARAM_IDS),
                        (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING),
                        (boolean) params.get(SsoConstants.HTTP_PARAM_GROUPS_RESOLVING_RECURSIVE))));
        return results;
    }

    private static List<Map<String, Object>> getProfileList(SsoContext ssoContext) {
        return AuthenticationUtils.getProfileList(ssoContext.getSsoExtensionsManager());
    }

    private static List<ExtMap> searchDirectoryUsers(SsoContext ssoContext, Map<String, Object> params) {
        log.debug("Entered searchDirectoryUsers");
        String authzName = (String) params.get("authz");
        String query = (String) params.get("query");
        ExtensionProxy authz = AuthenticationUtils.getExtensionProfileByAuthzName(ssoContext, authzName).getAuthz();

        List<ExtMap> results = new ArrayList<>();
        getNamespaces(ssoContext, (String) params.get("namespace"), authzName).
                forEach((namespace) -> results.addAll(
                        DirectoryUtils.findDirectoryUsersByQuery(authz,
                                namespace,
                                query)));

        log.debug("DirectoryUtils.findDirectoryUsersByQuery returned {} users in authz {} for query {}",
                results.size(),
                authzName,
                query);
        return results;
    }

    private static List<String> getNamespaces(SsoContext ssoContext, String authzName) {
        Map<String, List<String>> namespacesMap = AuthenticationUtils.getAvailableNamesSpaces(
                ssoContext.getSsoExtensionsManager());
        return namespacesMap.get(authzName);
    }

    private static List<String> getNamespaces(SsoContext ssoContext, String namespace, String authz) {
        log.debug("Entered getNamespaces");
        List<String> namespaces;
        if (StringUtils.isNotEmpty(namespace)) {
            namespaces = Arrays.asList(namespace);
        } else {
            namespaces = getNamespaces(ssoContext, authz);
        }
        log.debug("getNamespaces found {} namespaces in authz {}",
                namespaces == null ? 0 : namespaces.size(),
                authz);
        return namespaces == null ? Collections.<String> emptyList() : namespaces;
    }
}
