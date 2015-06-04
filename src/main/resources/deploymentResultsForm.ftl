${webResourceManager.requireResource("au.com.agic.bamboo_mass_deploy:bamboo_mass_deploy-resources")}

<div class="plugin-mass-deploy-container" data-provide="plugin-mass-deploy-page" data-page="results">

	<h1>Deployments in progress...</h1>

	<p>Deployments for current projects are running in background</p>

	<table class="aui">

		<thead>
			<tr>
				<th>Deployment project</th>
				<th>Environment</th>
				<th>Release</th>
				<th>Result</th>
			</tr>
		</thead>

	[#list deploymentObjects as deployment]

		<tr>
			<td>
				<a target="_blank"
				   href="${baseUrl}/deploy/viewDeploymentProjectEnvironments.action?id=${deployment.project.id}">
					${deployment.project.name}
				</a>
			</td>
			<td>
				<a target="_blank" href="${baseUrl}/deploy/viewEnvironment.action?id=${deployment.targetEnvironment.id}">
					${deployment.targetEnvironment.name}
				</a>
			</td>
			<td>
				<a target="_blank" href="${baseUrl}/deploy/viewDeploymentVersion.action?versionId=${deployment.version.id}">
					${deployment.version.name}
				</a>
			</td>
			<td>
				<span class="status-container">
					<span data-provide="result-status"
						  data-url="${baseUrl}/deploymentVersionJSON.action?version=${deployment.result.key.resultNumber}">
						Waiting...
					</span>
					<i class="three-quarters"></i>
				</span>

				<a href="${baseUrl}/deploy/viewDeploymentResult.action?deploymentResultId=${deployment.result.key.resultNumber}">
					#${deployment.result.key.resultNumber}
				</a>
			</td>
		</tr>

	[/#list]

	</table>

</div>
