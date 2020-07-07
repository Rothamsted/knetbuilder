package net.sourceforge.ondex.init;

import java.util.Arrays;

/**
 * Bean that contains metadata on producer
 *
 * @author lysenkoa
 */
public class PluginDescription {

    private String moduleId;
    private String ondexTypeName;
    private PluginType ondexType;   //type of producer implemented
    private String ondexId;         //id of the producer implemented

    private String GUIType;
    private String path;

    private String version;
    private String name;
    private String description;
    protected String cls;
    private String method;
    private String methodArgs;

    protected ArgumentDescription[] argDef;

    public PluginDescription() {
    }

    public String getOndexTypeName() {
		return ondexTypeName;
	}

	public void setOndexTypeName(String ondexTypeName) {
		this.ondexTypeName = ondexTypeName;
	}

	/**
     * @return the type of producer implemented
     */
    public PluginType getOndexType() {
        return ondexType;
    }

    /**
     * @param ondexType the type of producer implemented
     */
    public void setOndexType(PluginType ondexType) {
        this.ondexType = ondexType;
    }

    /**
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the unique id of the producer implemented
     */
    public String getOndexId() {
        return ondexId;
    }

    /**
     * @param ondexId the unique id of the producer implemented
     */
    public void setOndexId(String ondexId) {
        this.ondexId = ondexId;
    }

    /**
     * @return
     */
    public String getGUIType() {
        return GUIType;
    }

    /**
     * @param type
     */
    public void setGUIType(String type) {
        this.GUIType = type;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public String getCls() {
        return cls;
    }

    /**
     * @param cls
     */
    public void setCls(String cls) {
        this.cls = cls;
    }

    /**
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return
     */
    public String getMethodArgs() {
        return methodArgs;
    }

    /**
     * @param methodArgs
     */
    public void setMethodArgs(String methodArgs) {
        this.methodArgs = methodArgs;
    }

    /**
     * @return
     */
    public ArgumentDescription[] getArgDef() {
        return argDef;
    }

    /**
     * @param argDef
     */
    public void setArgDef(ArgumentDescription[] argDef) {
        this.argDef = argDef;
    }

    /**
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * @param moduleId
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("PluginDescription");
        sb.append("{moduleId='").append(moduleId).append('\'');
        sb.append(", ondexTypeName='").append(ondexTypeName).append('\'');
        sb.append(", ondexType=").append(ondexType);
        sb.append(", ondexId='").append(ondexId).append('\'');
        sb.append(", GUIType='").append(GUIType).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", cls='").append(cls).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", methodArgs='").append(methodArgs).append('\'');
        sb.append(", argDef=").append(argDef == null ? "null" : Arrays.asList(argDef).toString());
        sb.append('}');
        return sb.toString();
    }
}