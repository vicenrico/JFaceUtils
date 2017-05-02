package it.albertus.httpserver;

public abstract class AbstractStaticHandler extends AbstractHttpHandler {

	private String basePath;
	private String cacheControl;
	private boolean attachment;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(final String basePath) {
		this.basePath = normalizeBasePath(basePath);
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCacheControl(final String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public boolean isAttachment() {
		return attachment;
	}

	public void setAttachment(final boolean attachment) {
		this.attachment = attachment;
	}

	protected abstract String normalizeBasePath(String basePath);

}