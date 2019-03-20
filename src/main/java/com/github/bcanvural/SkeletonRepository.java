package com.github.bcanvural;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.hippoecm.hst.core.jcr.RuntimeRepositoryException;
import org.hippoecm.repository.security.HippoSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.bloomreach.ps.brxm.jcr.repository.utils.ImporterUtils;

import static org.hippoecm.repository.api.HippoNodeType.HIPPO_PATHS;

// TODO: Decide which yamls should be provided by lib / client
//  Decide How to use InmemoryJcrRepository
//  Should the use of hipposecuritymanager be optional? (adds +2 seconds or so)
//  Revisit jmvenabledusers. (or even session-pools)

public class SkeletonRepository extends InMemoryJcrRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkeletonRepository.class);

    private List<String> cndResourcesPatterns;
    private List<String> yamlResourcesPatterns;
    private List<String> contributedYamlResourcesPatterns;

    public SkeletonRepository(List<String> cndResourcesPatterns, List<String> contributedCndResourcesPatterns,
                              List<String> yamlResourcesPatterns, List<String> contributedYamlResourcesPatterns)
            throws RepositoryException, IOException {

        this.cndResourcesPatterns = cndResourcesPatterns;
        this.cndResourcesPatterns.addAll(contributedCndResourcesPatterns);

        this.yamlResourcesPatterns = yamlResourcesPatterns;
        this.contributedYamlResourcesPatterns = contributedYamlResourcesPatterns;

    }

    public void init() throws Exception {
        Session session = null;
        try {

            Session systemSession = originalRepository.getRootSession("default").impersonate(new SimpleCredentials("system", "".toCharArray()));
            registerCnds(systemSession, cndResourcesPatterns);
            importYamlResources(systemSession, yamlResourcesPatterns);

            HippoSecurityManager securityManager = (HippoSecurityManager) originalRepository.getSecurityManager();
            originalRepository.getSecurityManager().init(originalRepository, originalRepository.getRootSession("default"));
            securityManager.configure();


            session = this.login(new SimpleCredentials("admin", "admin".toCharArray()));

            registerCnds(session, cndResourcesPatterns);
            importYamlResources(session, contributedYamlResourcesPatterns);
            importTemplates(session);
            recalculateHippoPaths("/content");


        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    private void importYamlResources(Session session, List<String> yamlResourcePatterns) throws RepositoryException {
        try {
            for (String yamlResourcePattern : yamlResourcePatterns) {
                Resource[] resources = resolveResourcePattern(yamlResourcePattern);
                for (Resource resource : resources) {
                    ImporterUtils.importYaml(resource.getURL(), session.getRootNode(),
                            "", "hippostd:folder");
                }
            }
            session.save();

        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    private void importTemplates(Session session) throws RepositoryException {
        try {
            Resource[] resources = resolveResourcePattern("classpath*:com/github/bcanvural/imports/templates.yaml");
            for (Resource resource : resources) {
                ImporterUtils.importYaml(resource.getURL(), session.getRootNode(),
                        "/hippo:configuration", "hippostd:folder");
            }
            session.save();
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    private void registerCnds(Session session, List<String> cndResourcesPatterns) throws RepositoryException {
        for (String cndResourcePattern : cndResourcesPatterns) {
            registerNamespaces(session, resolveResourcePattern(cndResourcePattern));
        }
    }

    private void registerNamespaces(Session session, Resource[] cndResources) throws RepositoryException {
        for (Resource cndResource : cndResources) {
            try {
                // Register the custom node types defined in the CND file, using JCR Commons CndImporter
                NodeType[] nodeTypes = CndImporter.registerNodeTypes(new InputStreamReader(cndResource.getInputStream()), session);
                for (NodeType nt : nodeTypes) {
                    LOGGER.debug("Registered: " + nt.getName());
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }
    }

    private Resource[] resolveResourcePattern(String pattern) throws RepositoryException {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                LOGGER.debug("RESOURCE: " + resource.getFilename());
            }
            return resources;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Recalculating hippo paths is necessary for the HstQueries
     *
     * @param absolutePath
     */

    private void recalculateHippoPaths(String absolutePath) {
        Session session = null;
        try {
            session = this.login(new SimpleCredentials("admin", "admin".toCharArray()));
            Node rootNode = session.getRootNode();
            Node node = rootNode.getNode(absolutePath.substring(1));
            calculateHippoPaths(node, getPathsForNode(node, rootNode));
            session.save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    private LinkedList<String> getPathsForNode(Node node, Node rootNode) throws RepositoryException {
        LinkedList<String> paths = new LinkedList<>();
        Node parentNode = node;
        do {
            parentNode = parentNode.getParent();
            paths.add(parentNode.getIdentifier());
        } while (!parentNode.isSame(rootNode));
        return paths;
    }

    @SuppressWarnings("unchecked")
    private void calculateHippoPaths(Node node, LinkedList<String> paths) throws RepositoryException {
        paths.add(0, node.getIdentifier());
        setHippoPath(node, paths);
        for (NodeIterator nodes = node.getNodes(); nodes.hasNext(); ) {
            Node subnode = nodes.nextNode();
            if (!subnode.isNodeType("hippo:handle")) {
                if (!subnode.isNodeType("hippotranslation:translations")) {
                    calculateHippoPaths(subnode, (LinkedList<String>) paths.clone());
                }
            } else {
                setHandleHippoPaths(subnode, (LinkedList<String>) paths.clone());
            }
        }
    }

    private void setHippoPath(Node node, LinkedList<String> paths) throws RepositoryException {
        node.setProperty(HIPPO_PATHS, paths.toArray(new String[0]));
    }

    private void setHandleHippoPaths(Node handle, LinkedList<String> paths) throws RepositoryException {
        paths.add(0, handle.getIdentifier());
        for (NodeIterator nodes = handle.getNodes(handle.getName()); nodes.hasNext(); ) {
            Node subnode = nodes.nextNode();
            paths.add(0, subnode.getIdentifier());
            setHippoPath(subnode, paths);
            paths.remove(0);
        }
    }
}
