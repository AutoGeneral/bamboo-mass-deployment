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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Mass deployment setup form controller
 */
public class DeploymentSetupForm extends BambooActionSupport {

	private static final String DEFAULT_ENV_FILTER = "AWS";
	private static final long serialVersionUID = 2365222749104955291L;

	private final DeploymentProjectService deploymentProjectService;
	private final DeploymentResultService deploymentResultService;
	private final EnvironmentService environmentService;

	private final String baseUrl;

	private String fromEnv = "";
	private String toEnv = "";
	private String filter = "";

	private List<String> environmentsList = new ArrayList<>();
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

	public String getFilter() {
		return filter;
	}

	@SuppressWarnings("unused")
	public List<String> getEnvironmentsList() {
		return environmentsList;
	}

	public DeploymentSetupForm(final DeploymentProjectService deploymentProjectService,
		final EnvironmentService environmentService,
		final DeploymentResultService deploymentResultService,
		final AdministrationConfigurationAccessor configurationAccessor) {

		this.deploymentProjectService = deploymentProjectService;
		this.deploymentResultService = deploymentResultService;
		this.environmentService = environmentService;

		environmentsList = getAllEnvironments();
		baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();
	}

	@Override
	public String doDefault() throws Exception {
		setEnvVariables();

		List<DeploymentProject> deploymentProjectsList = deploymentProjectService.getAllDeploymentProjects();
		deploymentObjects = new ArrayList<>();

		for (DeploymentProject deploymentProject : deploymentProjectsList) {
			List<Environment> environments =
				environmentService.getEnvironmentsForDeploymentProject(deploymentProject.getId());

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
				DeploymentResult deploymentResult =
					deploymentResultService.getLatestDeploymentResultForEnvironment(fromEnvironment.getId());

				if (deploymentResult != null) {
					DeploymentVersion deploymentVersion = deploymentResult.getDeploymentVersion();
					if (deploymentVersion != null) {
						DeploymentObject deploymentObject =
							new DeploymentObject(deploymentProject, toEnvironment, deploymentVersion, null);
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
	private List<String> getAllEnvironments() {
		List<String> list = new ArrayList<>();

		try {
			Iterable<Environment> environments = environmentService.getAllEnvironments();
			for (Environment environment : environments) {
				list.add(environment.getName());
			}
		} catch (Exception ex) {
			// Not authorised
		}

		Collections.sort(list);

		return list;
	}

	/**
	 * Parse query string params 'fromEnv' and 'toEnv' and populate
	 * related object's fields
	 */
	private void setEnvVariables() {
		final HttpServletRequest request = ServletActionContext.getRequest();
		final String fromEnvRaw = request.getParameter("fromEnv");
		final String toEnvRaw = request.getParameter("toEnv");
		final String filterRaw = request.getParameter("filter");

		if (filterRaw != null) {
			filter = filterRaw;
		} else {
			filter = DEFAULT_ENV_FILTER;
		}

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
