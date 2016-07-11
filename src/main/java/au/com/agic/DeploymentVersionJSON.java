package au.com.agic;

import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.webwork.dispatcher.json.JSONException;
import com.opensymphony.webwork.dispatcher.json.JSONObject;
import com.opensymphony.xwork2.Action;

import org.apache.struts2.ServletActionContext;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/**
 * Proxy-class that exposes endpoint to get JSON status for deployment version
 * Version ID is expected to be a value for 'version' query parameter
 * <p/>
 * In case of any error, returns json object
 * {
 * status: "error",
 * message: "...."
 * }
 * <p/>
 * Otherwise returns the positive result, for example
 * {
 * status: "OK",
 * life_cycle_state: "InProgress",
 * deployment_state: "Unknown"
 * }
 */
public class DeploymentVersionJSON extends BambooActionSupport {

	private static final long serialVersionUID = -122842815102740306L;
	private final DeploymentResultService deploymentResultService;
	private final JSONObject jsonObject = new JSONObject();

	public DeploymentVersionJSON(final DeploymentResultService deploymentResultService) {
		this.deploymentResultService = deploymentResultService;
	}

	private void errorResponse(final String message) throws JSONException {
		jsonObject.put("status", "error");
		jsonObject.put("message", message);
	}

	@NotNull
	@Override
	public JSONObject getJsonObject() throws JSONException {
		HttpServletRequest request = ServletActionContext.getRequest();
		Long versionId = null;

		try {
			versionId = Long.parseLong(request.getParameter("version"), 10);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}

		if (versionId == null) {
			errorResponse("Version Id not specified");
			return jsonObject;
		}

		DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(versionId);

		if (deploymentResult == null) {
			errorResponse("No deployment result for this Id");
			return jsonObject;
		}

		jsonObject.put("status", "OK");
		jsonObject.put("life_cycle_state", deploymentResult.getLifeCycleState().toString());
		jsonObject.put("deployment_state", deploymentResult.getDeploymentState().toString());

		return jsonObject;
	}

	@Override
	public String doDefault() throws Exception {
		return Action.SUCCESS;
	}
}
