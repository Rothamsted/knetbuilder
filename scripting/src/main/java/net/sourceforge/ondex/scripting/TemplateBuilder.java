package net.sourceforge.ondex.scripting;

public interface TemplateBuilder<T extends ProxyTemplate> extends ProxyTemplateDependant {

	/**
	 * 
	 * @return proxy template with appropriate functions and wrappers
	 */
	public abstract T getProxyTemplate();
	public abstract Class<T> getProxyTemplateType();
}
