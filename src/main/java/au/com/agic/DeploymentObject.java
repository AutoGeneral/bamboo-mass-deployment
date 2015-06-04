package au.com.agic;

import com.atlassian.bamboo.core.BambooIdProvider;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.projects.DeploymentProject;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;

/**
 * Deployment object encapsulates some Bamboo entities and helps us
 * to easily pass it through pages (manual serialize and deserialize)
 */
public class DeploymentObject {

	private DeploymentProject project;
	private Environment targetEnvironment;
	private DeploymentVersion version;
	private DeploymentResult result;
	private String code;

	public Environment getTargetEnvironment() {
		return targetEnvironment;
	}

	public void setTargetEnvironment(Environment targetEnvironment) {
		this.targetEnvironment = targetEnvironment;
	}

	public DeploymentVersion getVersion() {
		return version;
	}

	public void setVersion(DeploymentVersion version) {
		this.version = version;
	}

	public DeploymentProject getProject() {
		return project;
	}

	public void setProject(DeploymentProject project) {
		this.project = project;
	}

	public DeploymentResult getResult() {
		return result;
	}

	public void setResult(DeploymentResult result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public DeploymentObject(DeploymentProject project, Environment targetEnvironment, DeploymentVersion version, DeploymentResult result) {
		this.project = project;
		this.targetEnvironment = targetEnvironment;
		this.version = version;
		this.result = result;
	}

	private String getSerializedId(BambooIdProvider entity) {
		if (entity != null) {
			return Long.toString(entity.getId());
		}
		return "null";
	}

	public String serialize() {
		code = getSerializedId(project);
		code += ":" + getSerializedId(targetEnvironment);
		code += ":" + getSerializedId(version);
		code += ":" + getSerializedId(result);
		return code;
	}
}
