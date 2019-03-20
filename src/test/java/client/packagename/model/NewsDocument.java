package client.packagename.model;

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hippoecm.hst.content.beans.ContentNodeBinder;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "newsdocument")
@XmlAccessorType(XmlAccessType.NONE)
@HippoEssentialsGenerated(internalName = "myhippoproject:newsdocument")
@Node(jcrType = "myhippoproject:newsdocument")
public class NewsDocument extends HippoDocument implements ContentNodeBinder {

    private static Logger LOGGER = LoggerFactory.getLogger(client.packagename.beans.NewsDocument.class);

    /**
     * The document type of the news document.
     */
    public static final String DOCUMENT_TYPE = "myhippoproject:newsdocument";

    private static final String TITLE = "myhippoproject:title";
    private static final String DATE = "myhippoproject:date";
    private static final String INTRODUCTION = "myhippoproject:introduction";
    private static final String IMAGE = "myhippoproject:image";
    private static final String CONTENT = "myhippoproject:content";
    private static final String LOCATION = "myhippoproject:location";
    private static final String AUTHOR = "myhippoproject:author";
    private static final String SOURCE = "myhippoproject:source";

    private String title;
    private String introduction;

    @JsonProperty(value = "title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty(value = "introduction")
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    /**
     * Get the title of the document.
     *
     * @return the title
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:title")
    public String getTitle() {
        return (title == null) ? getProperty(TITLE) : title;
    }

    /**
     * Get the date of the document.
     *
     * @return the date
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:date")
    public Calendar getDate() {
        return getProperty(DATE);
    }

    /**
     * Get the introduction of the document.
     *
     * @return the introduction
     */
    @HippoEssentialsGenerated(internalName = "myhippoproject:introduction")
    public String getIntroduction() {
        return (introduction == null) ? getProperty(INTRODUCTION) : introduction;
    }

    /**
     * Get the image of the document.
     *
     * @return the image
     */
    @HippoEssentialsGenerated(internalName = "myhippoproject:image")
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /**
     * Get the main content of the document.
     *
     * @return the content
     */
    @HippoEssentialsGenerated(internalName = "myhippoproject:content")
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /**
     * Get the location of the document.
     *
     * @return the location
     */
    @HippoEssentialsGenerated(internalName = "myhippoproject:location")
    public String getLocation() {
        return getProperty(LOCATION);
    }

    /**
     * Get the author of the document.
     *
     * @return the author
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:author")
    public String getAuthor() {
        return getProperty(AUTHOR);
    }

    /**
     * Get the source of the document.
     *
     * @return the source
     */
    @XmlElement
    @HippoEssentialsGenerated(internalName = "myhippoproject:source")
    public String getSource() {
        return getProperty(SOURCE);
    }

    @Override
    public boolean bind(final Object content, final javax.jcr.Node node) throws ContentNodeBindingException {
        if(content instanceof client.packagename.beans.NewsDocument) {
            client.packagename.beans.NewsDocument newsDocument = (client.packagename.beans.NewsDocument) content;
            try {
                node.setProperty(TITLE, newsDocument.getTitle());
                node.setProperty(DATE, newsDocument.getDate());
                node.setProperty(INTRODUCTION, newsDocument.getIntroduction());
            } catch (RepositoryException e) {
                LOGGER.error("Unable to bind the content to the JCR Node" + e.getMessage(), e);
                throw new ContentNodeBindingException(e);
            }
            return true;
        }

        return false;
    }

}

