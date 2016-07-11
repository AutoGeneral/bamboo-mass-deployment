package au.com.agic;

import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.execution.service.DeploymentExecutionService;
import com.atlassian.bamboo.deployments.execution.triggering.EnvironmentTriggeringAction;
import com.atlassian.bamboo.deployments.execution.triggering.EnvironmentTriggeringActionFactory;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionService;
import com.atlassian.bamboo.plan.ExecutionRequestResult;
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.user.User;
import com.opensymphony.xwork2.Action;

import org.apache.struts2.ServletActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Deployment execution controller
 * <p/>
 * This page controller receives 'params' query parameter with the list of serialized
 * DeploymentObjects, parses it and then executes deployments
 * <p/>
 * Exposes resultsParamString field with the serialized list of DeploymentObjects
 */
public class DeploymentExecution extends BambooActionSupport {

	private static final long serialVersionUID = 7830762978792971693L;

	private final DeploymentExecutionService deploymentExecutionService;
	private final EnvironmentService environmentService;
	private final DeploymentVersionService deploymentVersionService;
	private final BambooAuthenticationContext bambooAuthenticationContext;
	private final DeploymentResultService deploymentResultService;
	private final EnvironmentTriggeringActionFactory triggeringActionFactory;

	/**
	 * String we will return with the list of serialized DeploymentObjects
	 */
	private String resultsParamString;

	public DeploymentExecution(
		final DeploymentExecutionService deploymentExecutionService,
		final EnvironmentService environmentService,
		final DeploymentVersionService deploymentVersionService,
		final BambooAuthenticationContext bambooAuthenticationContext,
		final DeploymentResultService deploymentResultService,
		final EnvironmentTriggeringActionFactory triggeringActionFactory) {

		this.deploymentExecutionService = deploymentExecutionService;
		this.environmentService = environmentService;
		this.deploymentVersionService = deploymentVersionService;
		this.bambooAuthenticationContext = bambooAuthenticationContext;
		this.deploymentResultService = deploymentResultService;
		this.triggeringActionFactory = triggeringActionFactory;
	}

	/**
	 * Deserialize the list of DeploymentObjects and return the actual list of created objects
	 *
	 * @param serializedString - serialized list of DeploymentObjects
	 * @return list of deserialized DeploymentObjects
	 */
	private List<DeploymentObject> getDeploymentInfoFromParams(String serializedString) {
		final String[] parts = serializedString.split(Pattern.quote(";"));
		List<DeploymentObject> result = new ArrayList<>();

		for (String part : parts) {
			String[] param = part.split(Pattern.quote(":"));
			if (param.length >= 3) {
				Environment environment = environmentService.getEnvironment(Long.parseLong(param[1], 10));
				DeploymentVersion deploymentVersion = deploymentVersionService.getDeploymentVersion(Long.parseLong(param[2], 10));

				DeploymentObject deploymentObject = new DeploymentObject(null, environment, deploymentVersion, null);
				deploymentObject.serialize();

				result.add(deploymentObject);
			}
		}
		return result;
	}

	@Override
	public String doDefault() throws Exception {
		resultsParamString = "";
		List<DeploymentObject> deploymentObjects = null;
		final HttpServletRequest request = ServletActionContext.getRequest();
		final String rawParams = request.getParameter("params");

		if (rawParams != null) {
			deploymentObjects = getDeploymentInfoFromParams(rawParams);
		}
		if (deploymentObjects == null) {
			return Action.ERROR;
		}

		for (DeploymentObject deploymentObject : deploymentObjects) {
			// Create deployment context and start deployment
			User user = bambooAuthenticationContext.getUser();

			EnvironmentTriggeringAction environmentTriggeringAction =
				triggeringActionFactory.createManualEnvironmentTriggeringAction(
					deploymentObject.getTargetEnvironment(), deploymentObject.getVersion(), user);

			ExecutionRequestResult executionRequestResult =
				deploymentExecutionService.execute(deploymentObject.getTargetEnvironment(), environmentTriggeringAction);

			if (executionRequestResult.getDeploymentResultId() != null) {
				DeploymentResult deploymentResult =
					deploymentResultService.getDeploymentResult(executionRequestResult.getDeploymentResultId());
				deploymentObject.setResult(deploymentResult);

				// Generate the response string
				resultsParamString += deploymentObject.serialize() + ";";
			}
		}
		return Action.SUCCESS;
	}

	@SuppressWarnings("unused")
	public String getResultsParamString() {
		return resultsParamString;
	}
}

