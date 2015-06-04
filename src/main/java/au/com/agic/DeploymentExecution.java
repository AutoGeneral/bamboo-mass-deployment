package au.com.agic;

import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.execution.DeploymentContext;
import com.atlassian.bamboo.deployments.execution.service.DeploymentExecutionService;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionService;
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.xwork2.Action;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Deployment execution controller
 * <p/>
 * This page controller receives 'params' query parameter with the list of serialized DeploymentObjects,
 * parses it and then executes deployments
 * <p/>
 * Exposes resultsParamString field with the serialized list of DeploymentObjects
 */
public class DeploymentExecution extends BambooActionSupport {

	private final DeploymentExecutionService deploymentExecutionService;
	private final EnvironmentService environmentService;
	private final DeploymentVersionService deploymentVersionService;
	private final BambooAuthenticationContext bambooAuthenticationContext;
	private final DeploymentResultService deploymentResultService;

	/**
	 * String we will return with the list of serialized DeploymentObjects
	 */
	private String resultsParamString;

	public DeploymentExecution(DeploymentExecutionService deploymentExecutionService, EnvironmentService environmentService,
							   DeploymentVersionService deploymentVersionService, BambooAuthenticationContext bambooAuthenticationContext,
							   DeploymentResultService deploymentResultService) {
		this.deploymentExecutionService = deploymentExecutionService;
		this.environmentService = environmentService;
		this.deploymentVersionService = deploymentVersionService;
		this.bambooAuthenticationContext = bambooAuthenticationContext;
		this.deploymentResultService = deploymentResultService;
	}

	/**
	 * Deserialize the list of DeploymentObjects and return the actual list of created objects
	 *
	 * @param serializedString - serialized list of DeploymentObjects
	 * @return list of deserialized DeploymentObjects
	 */
	private List<DeploymentObject> getDeploymentInfoFromParams(String serializedString) {
		final String[] parts = serializedString.split(Pattern.quote(";"));
		List<DeploymentObject> result = new ArrayList<DeploymentObject>();

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

	/**
	 * We need to create custom Trigger Reason for deployments
	 * and add there information about the user triggered it
	 * (eg. 'Manual run from the stage: Mass Deployment by Aleksandr Shteinikov')
	 *
	 * @return trigger reason prepared to use
	 */
	private ManualBuildTriggerReason createTriggerReason() {
		ManualBuildTriggerReason triggerReason = new ManualBuildTriggerReason();
		triggerReason.setTextProvider(super.getTextProvider());

		String username = bambooAuthenticationContext.getUserName();

		Map<String, String> fields = new HashMap<String, String>();
		fields.put(ManualBuildTriggerReason.TRIGGER_MANUAL_USER, username);
		fields.put(ManualBuildTriggerReason.TRIGGER_MANUAL_STAGE, "Mass Deployment");
		String key = ManualBuildTriggerReason.KEY;

		triggerReason.init(key, fields);

		return triggerReason;
	}

	@Override
	public String doDefault() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		List<DeploymentObject> deploymentObjects = new ArrayList<DeploymentObject>();

		resultsParamString = "";

		String rawParams = request.getParameter("params");
		if (rawParams != null) {
			deploymentObjects = getDeploymentInfoFromParams(rawParams);
		}
		if (deploymentObjects != null) {
			// We will have only one trigger reason for all the deployments
			ManualBuildTriggerReason triggerReason = createTriggerReason();

			for (DeploymentObject deploymentObject : deploymentObjects) {

				// Create deployment context and start deployment
				DeploymentContext deploymentContext =
					deploymentExecutionService.prepareDeploymentContext(deploymentObject.getTargetEnvironment(), deploymentObject.getVersion(), triggerReason);

				deploymentExecutionService.execute(deploymentContext);

				DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentContext.getDeploymentResultId());
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

