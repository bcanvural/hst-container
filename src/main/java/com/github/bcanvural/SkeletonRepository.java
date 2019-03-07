package com.github.bcanvural;

import java.io.IOException;
import java.util.LinkedList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.hippoecm.hst.core.jcr.RuntimeRepositoryException;

import com.bloomreach.brxm.jcr.repository.InMemoryJcrRepository;
import com.bloomreach.brxm.jcr.repository.utils.NodeTypeUtils;
import com.bloomreach.brxm.jcr.repository.utils.YamlImporter;
import com.github.bcanvural.HstApp;

import static org.hippoecm.repository.api.HippoNodeType.HIPPO_PATHS;

public class SkeletonRepository extends InMemoryJcrRepository {

    private String[] resourceLocations;

    public SkeletonRepository() throws RepositoryException, IOException {
    }

    public void init() {
        Session session = null;
        try {
            session = this.login(new SimpleCredentials("admin", "admin".toCharArray()));
            registerNamespaces(session);
            for (String resourceLocation : resourceLocations) {
                YamlImporter.importPlainYaml(HstApp.class.getClassLoader()
                                .getResourceAsStream(resourceLocation), session.getRootNode(),
                        "", "hippostd:folder");
            }
            session.save();
            recalculateHippoPaths("/content");
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    public String[] getResourceLocations() {
        return resourceLocations;
    }

    public void setResourceLocations(final String[] resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

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

    private static void registerNamespaces(Session session) {
        try {
            NodeTypeUtils.createNodeType(session, "hst:hst");
            NodeTypeUtils.createNodeType(session, "hst:hst");
            NodeTypeUtils.createNodeType(session, "hst:formdatacontainer");
            NodeTypeUtils.createNodeType(session, "hst:configuration");
            NodeTypeUtils.createNodeType(session, "hst:configurations");
            NodeTypeUtils.createNodeType(session, "hst:pages");
            NodeTypeUtils.createNodeType(session, "hst:blueprints");
            NodeTypeUtils.createNodeType(session, "hst:channels");
            NodeTypeUtils.createNodeType(session, "hst:sites");
            NodeTypeUtils.createNodeType(session, "hst:virtualhosts");
            NodeTypeUtils.createNodeType(session, "hst:catalog");
            NodeTypeUtils.createNodeType(session, "hst:component");
            NodeTypeUtils.createNodeType(session, "hst:components");
            NodeTypeUtils.createNodeType(session, "hst:template");
            NodeTypeUtils.createNodeType(session, "hst:templates");
            NodeTypeUtils.createNodeType(session, "hst:sitemenus");
            NodeTypeUtils.createNodeType(session, "hst:sitemenu");
            NodeTypeUtils.createNodeType(session, "hst:sitemenuitem");
            NodeTypeUtils.createNodeType(session, "hst:sitemapitemhandlers");
            NodeTypeUtils.createNodeType(session, "hst:sitemapitem");
            NodeTypeUtils.createNodeType(session, "hst:sitemap");
            NodeTypeUtils.createNodeType(session, "hst:containeritempackage");
            NodeTypeUtils.createNodeType(session, "hst:containeritemcomponent");
            NodeTypeUtils.createNodeType(session, "hst:containercomponentreference");
            NodeTypeUtils.createNodeType(session, "hst:workspace");
            NodeTypeUtils.createNodeType(session, "hst:channel");
            NodeTypeUtils.createNodeType(session, "hst:channelinfo");
            NodeTypeUtils.createNodeType(session, "hst:containercomponentfolder");
            NodeTypeUtils.createNodeType(session, "hst:containercomponent");
            NodeTypeUtils.createNodeType(session, "hst:virtualhostgroup");
            NodeTypeUtils.createNodeType(session, "hst:virtualhost");
            NodeTypeUtils.createNodeType(session, "hst:mount");
            NodeTypeUtils.createNodeType(session, "hst:site");

            NodeTypeUtils.createNodeType(session, "hipposys:configuration");
            NodeTypeUtils.createNodeType(session, "hipposys:derivativesfolder");
            NodeTypeUtils.createNodeType(session, "hipposys:deriveddefinition");
            NodeTypeUtils.createNodeType(session, "hipposys:propertyreferences");
            NodeTypeUtils.createNodeType(session, "hipposys:builtinpropertyreference");
            NodeTypeUtils.createNodeType(session, "hipposys:relativepropertyreference");
            NodeTypeUtils.createNodeType(session, "hipposys:resolvepropertyreference");
            NodeTypeUtils.createNodeType(session, "hipposys:userfolder");
            NodeTypeUtils.createNodeType(session, "hipposys:user");

            NodeTypeUtils.createNodeType(session, "hippostd:foldertype");
            NodeTypeUtils.createNodeType(session, "hippostd:gallerytype");
            NodeTypeUtils.createNodeType(session, "hippostd:html");
            NodeTypeUtils.createNodeType(session, "hippostd:folder");
            NodeTypeUtils.createNodeType(session, "hippotranslation:id");
            NodeTypeUtils.createNodeType(session, "hippo:handle");
            NodeTypeUtils.createNodeType(session, "myhippoproject:author");
            NodeTypeUtils.createNodeType(session, "myhippoproject:newsdocument");
            NodeTypeUtils.createNodeType(session, "hippostdpubwf:createdBy");
            NodeTypeUtils.createNodeType(session, "hippogallerypicker:imagelink", "hippo:facetselect");
            NodeTypeUtils.createNodeType(session, "resourcebundle:resourcebundle");
            NodeTypeUtils.createNodeType(session, "hippogallery:stdImageGallery");
            NodeTypeUtils.createNodeType(session, "hippogallery:imageset");
            NodeTypeUtils.createNodeType(session, "hippogallery:image");
            NodeTypeUtils.createNodeType(session, "hippogallery:stdAssetGallery");

            NodeTypeUtils.createMixin(session, "hippotranslation:translated");
            NodeTypeUtils.createMixin(session, "hippo:named");


        } catch (Exception e) {

        }
    }
}
