package au.com.agic;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.projects.DeploymentProject;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionService;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.xwork2.Action;

import org.apache.struts2.ServletActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for page with the deployments results
 * Deserialized list of DeploymentObjects is expected to be a value for 'results' query param
 */
public class DeploymentResultsForm extends BambooActionSupport {

	private static final long serialVersionUID = -5744223337014651509L;

	private final DeploymentProjectService deploymentProjectService;
	private final EnvironmentService environmentService;
	private final DeploymentVersionService deploymentVersionService;
	private final DeploymentResultService deploymentResultService;

	private final String baseUrl;
	private List<DeploymentObject> deploymentObjects = new ArrayList<DeploymentObject>();

	@Override
	public String getBaseUrl() {
		return baseUrl;
	}

	@SuppressWarnings("unused")
	public List<DeploymentObject> getDeploymentObjects() {
		return deploymentObjects;
	}

	public DeploymentResultsForm(
		final AdministrationConfigurationAccessor configurationAccessor,
		final EnvironmentService environmentService,
		final DeploymentVersionService deploymentVersionService,
		final DeploymentResultService deploymentResultService,
		final DeploymentProjectService deploymentProjectService) {

		this.environmentService = environmentService;
		this.deploymentVersionService = deploymentVersionService;
		this.deploymentResultService = deploymentResultService;
		this.deploymentProjectService = deploymentProjectService;

		baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();
	}

	/**
	 * Deserialize the list of DeploymentObjects and return the actual list of created objects
	 *
	 * @param serializedString - serialized list of DeploymentObjects
	 * @return list of deserialized DeploymentObjects
	 */
	private List<DeploymentObject> getDeploymentInfoFromParams(final String serializedString) {
		final String[] parts = serializedString.split(Pattern.quote(";"));
		final List<DeploymentObject> result = new ArrayList<DeploymentObject>();

		for (final String part : parts) {
			final String[] param = part.split(Pattern.quote(":"));
			if (param.length >= 4) {

				final Environment environment =
					environmentService.getEnvironment(Long.parseLong(param[1], 10));
				final DeploymentVersion deploymentVersion =
					deploymentVersionService.getDeploymentVersion(Long.parseLong(param[2], 10));
				final DeploymentResult deploymentResult =
					deploymentResultService.getDeploymentResult(Long.parseLong(param[3], 10));

				DeploymentProject deploymentProject = null;
				if (environment != null) {
					deploymentProject =
						deploymentProjectService.getDeploymentProject(environment.getDeploymentProjectId());
				}

				DeploymentObject deploymentObject =
					new DeploymentObject(deploymentProject, environment, deploymentVersion, deploymentResult);
				
				deploymentObject.serialize();

				result.add(deploymentObject);
			}
		}
		return result;
	}

	@Override
	public String doDefault() throws Exception {
		final HttpServletRequest request = ServletActionContext.getRequest();
		final String rawParams = request.getParameter("results");

		if (rawParams != null) {
			deploymentObjects = getDeploymentInfoFromParams(rawParams);
		}

		return Action.SUCCESS;
	}

}
