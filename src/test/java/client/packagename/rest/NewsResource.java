package client.packagename.rest;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.annotations.Persistable;
import org.hippoecm.hst.content.beans.ObjectBeanPersistenceException;
import org.hippoecm.hst.content.beans.manager.workflow.BaseWorkflowCallbackHandler;
import org.hippoecm.hst.content.beans.manager.workflow.QualifiedWorkflowCallbackHandler;
import org.hippoecm.hst.content.beans.manager.workflow.WorkflowPersistenceManager;
import org.hippoecm.hst.content.beans.manager.workflow.WorkflowPersistenceManagerImpl;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.repository.api.WorkflowException;
import org.onehippo.cms7.essentials.components.paging.Pageable;
import org.onehippo.cms7.essentials.components.rest.BaseRestResource;
import org.onehippo.cms7.essentials.components.rest.ctx.DefaultRestContext;
import org.onehippo.cms7.essentials.components.rest.ctx.RestContext;
import org.onehippo.repository.documentworkflow.DocumentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.packagename.model.ListItemPagination;
import client.packagename.model.NewsDocument;
import client.packagename.model.NewsItemRep;


@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Path("/news/")
public class NewsResource extends BaseRestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsResource.class);

    @GET
    @Path("/")
    public Pageable<NewsItemRep> allNews(@Context HttpServletRequest request) {
        return findListItems(new DefaultRestContext(this, request));
    }

    private Pageable<NewsItemRep> findListItems(RestContext context) {
        ListItemPagination<NewsItemRep> pageable = new ListItemPagination<>();
        HstQuery query = createQuery(context, NewsDocument.class, Subtypes.EXCLUDE);
        try {
            HstQueryResult result = query.execute();

            pageable.setTotal(result.getTotalSize());
            pageable.setPageSize(context.getPageSize());
            pageable.setPageNumber(context.getPage());

            HippoBeanIterator it = result.getHippoBeans();
            it.forEachRemaining(hippoBean -> pageable.addItem(new NewsItemRep().represent((NewsDocument) hippoBean)));
        } catch (QueryException e) {
            LOGGER.warn(e.getLocalizedMessage());
        }
        return pageable;
    }

    @POST
    @Path("/create")
    public NewsDocument create(@Context HttpServletRequest request, final NewsDocument incomingBean) {
        NewsDocument createDocument = null;
        final HstRequestContext requestContext = getRequestContext(request);

        try {
            final WorkflowPersistenceManager wpm = getWorkflowPersistenceManager(requestContext.getSession());
            wpm.setWorkflowCallbackHandler(REQUEST_PUBLICATION_CALLBACK_HANDLER);

            final String createDocumentPath = createNewNode(getRequestContext(request), wpm, incomingBean.getTitle());

            createDocument = (NewsDocument) wpm.getObject(createDocumentPath);

            // copy the incoming data to the new document
            populateNewDocument(createDocument, incomingBean);

            if (createDocument != null) {
                wpm.update(createDocument);
            } else {
                LOGGER.error("Failed to add news for path: {}", createDocumentPath);
                try {
                    wpm.refresh();
                } catch (ObjectBeanPersistenceException obpe) {
                    LOGGER.warn("Failed to refresh: " + obpe.getMessage(), obpe);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to create news document", e);
        }
        return createDocument;

    }

    private String createNewNode(HstRequestContext context, WorkflowPersistenceManager wpm, final String newNodeName) throws ObjectBeanPersistenceException {
        final String newsFolderPath = createNewsFolderPath(context);
        return wpm.createAndReturn(newsFolderPath, NewsDocument.DOCUMENT_TYPE, newNodeName, true);
    }

    private String createNewsFolderPath(final HstRequestContext context) {
        return context.getResolvedMount().getMount().getContentPath() + "/news/restimport";
    }


    private void populateNewDocument(NewsDocument newDocument, NewsDocument incomingBean) {
        newDocument.setTitle(incomingBean.getTitle());
        newDocument.setIntroduction(incomingBean.getIntroduction());
    }

    private WorkflowPersistenceManager getWorkflowPersistenceManager(Session session) {
        return new WorkflowPersistenceManagerImpl(session, RequestContextProvider.get().getContentBeansTool().getObjectConverter());
    }

    private static final QualifiedWorkflowCallbackHandler<DocumentWorkflow> REQUEST_PUBLICATION_CALLBACK_HANDLER = new RequestPublicationCallbackHandler();

    private static class RequestPublicationCallbackHandler extends BaseWorkflowCallbackHandler<DocumentWorkflow> {
        @Override
        public void processWorkflow(DocumentWorkflow workflow) throws RemoteException, RepositoryException, WorkflowException {
            workflow.requestPublication();
        }
    }


}
