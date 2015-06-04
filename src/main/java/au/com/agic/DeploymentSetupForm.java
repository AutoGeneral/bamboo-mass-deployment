package au.com.agic;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.projects.DeploymentProject;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.xwork2.Action;
import org.apache.struts2.ServletActionContext;
import org.jetbrains.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Mass deployment setup form controller
 */
public class DeploymentSetupForm extends BambooActionSupport {
	private final DeploymentProjectService deploymentProjectService;
	private final DeploymentResultService deploymentResultService;
	private final EnvironmentService environmentService;

	private final String baseUrl;

	private String fromEnv = "";
	private String toEnv = "";

	private Set<String> environmentsList = new HashSet<String>();
	private List<DeploymentObject> deploymentObjects;

	@Nullable
	@Override
	@SuppressWarnings("unused")
	public String getBaseUrl() {
		return baseUrl;
	}

	@SuppressWarnings("unused")
	public List<DeploymentObject> getDeploymentObjects() {
		return deploymentObjects;
	}

	@SuppressWarnings("unused")
	public String getFromEnv() {
		return fromEnv;
	}

	@SuppressWarnings("unused")
	public String getToEnv() {
		return toEnv;
	}

	@SuppressWarnings("unused")
	public Set<String> getEnvironmentsList() {
		return environmentsList;
	}

	public DeploymentSetupForm(DeploymentProjectService deploymentProjectService,
							   EnvironmentService environmentService,
							   DeploymentResultService deploymentResultService,
							   AdministrationConfigurationAccessor administrationConfigurationAccessor) {

		this.deploymentProjectService = deploymentProjectService;
		this.deploymentResultService = deploymentResultService;
		this.environmentService = environmentService;

		environmentsList = getAllEnvironments();
		baseUrl = administrationConfigurationAccessor.getAdministrationConfiguration().getBaseUrl();
	}

	@Override
	public String doDefault() throws Exception {
		setEnvVariables();

		List<DeploymentProject> deploymentProjectsList = deploymentProjectService.getAllDeploymentProjects();
		deploymentObjects = new ArrayList<DeploymentObject>();

		for (DeploymentProject deploymentProject : deploymentProjectsList) {
			List<Environment> environments = environmentService.getEnvironmentsForDeploymentProject(deploymentProject.getId());

			Environment fromEnvironment = null;
			Environment toEnvironment = null;

			for (Environment environment : environments) {
				if (environment.getName().equals(fromEnv)) {
					fromEnvironment = environment;
				}
				if (environment.getName().equals(toEnv)) {
					toEnvironment = environment;
				}
			}

			if (fromEnvironment != null && toEnvironment != null) {
				DeploymentResult deploymentResult = deploymentResultService.getLatestDeploymentResultForEnvironment(fromEnvironment.getId());
				if (deploymentResult != null) {
					DeploymentVersion deploymentVersion = deploymentResult.getDeploymentVersion();
					if (deploymentVersion != null) {
						DeploymentObject deploymentObject = new DeploymentObject(deploymentProject, toEnvironment, deploymentVersion, null);
						deploymentObject.serialize();

						deploymentObjects.add(deploymentObject);
					}
				}
			}
		}

		return Action.SUCCESS;
	}

	/**
	 * Get the set of environments names used across all the projects
	 *
	 * @return set of environments names
	 */
	private Set<String> getAllEnvironments() {
		Set<String> environmentsSet = new HashSet<String>();

		try {
			Iterable<Environment> environments = environmentService.getAllEnvironments();
			for (Environment environment : environments) {
				environmentsSet.add(environment.getName());
			}
		} catch (Exception e) {
			// Not authorised
		}

		return environmentsSet;
	}

	/**
	 * Parse query string params 'fromEnv' and 'toEnv' and populate
	 * related object's fields
	 */
	private void setEnvVariables() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String fromEnvRaw = request.getParameter("fromEnv");
		String toEnvRaw = request.getParameter("toEnv");

		if (fromEnvRaw != null) {
			fromEnv = fromEnvRaw;
		} else {
			if (!environmentsList.isEmpty()) {
				fromEnv = environmentsList.iterator().next();
			}
		}
		if (toEnvRaw != null) {
			toEnv = toEnvRaw;
		} else {
			if (environmentsList.size() > 1) {
				Iterator iter = environmentsList.iterator();
				iter.next();
				toEnv = (String) iter.next();
			}
		}
	}
}
