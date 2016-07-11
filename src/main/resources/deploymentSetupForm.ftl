${webResourceManager.requireResource("au.com.agic.bamboo_mass_deploy:bamboo_mass_deploy-resources")}

<div class="plugin-mass-deploy-container" data-provide="plugin-mass-deploy-page" data-page="form">

	<h1>Mass Deployment Plugin</h1>

	[#if environmentsList?has_content]

		<form class="aui">

			<fieldset>

				<h3>Search by environments</h3>

				<div class="description">
					Search for deployment projects and their latest releases that can be promoted from one environment to
					another
				</div>

                <div class="field-group plugin-mass-deploy-environments">
                    <label>Filter environments</label>
                    <input type="text" name="filter" class="text" value="${filter}" data-provide="environment-filter"/>
                </div>

				<div class="field-group plugin-mass-deploy-environments">
					<label>From environment</label>
					<select class="select" name="fromEnv" data-provide="environment-list-from">
						[#list environmentsList as env]
							<option value="${env}" [#if env == fromEnv] selected [/#if]>${env}</option>
						[/#list]
					</select>
				</div>

                <div class="field-group plugin-mass-deploy-environments">
                    <label>To environment</label>
					<select class="select" name="toEnv" data-provide="environment-list-to">
						[#list environmentsList as env]
							<option value="${env}" [#if env == toEnv] selected [/#if]>${env}</option>
						[/#list]
					</select>
				</div>

				<div class="field-group plugin-mass-deploy-environments">
                    <input class="aui-button" type="submit" value="Search"/>
				</div>

			</fieldset>

		</form>

		<table class="aui">

			<thead>
			<tr>
				<th>Deployment project</th>
				<th>Latest version deployed to `${fromEnv}`</th>
				<th>Version creation date</th>
				<th>Promote to `${toEnv}`</th>
			</tr>
			</thead>

			<tbody>
				[#list deploymentObjects as deployment]

				<tr>
					<td>
						<a href="${baseUrl}/deploy/viewDeploymentProjectEnvironments.action?id=${deployment.project.id}">
							${deployment.project.name}
						</a>
					</td>

					<td>
						<a href="${baseUrl}/deploy/viewDeploymentVersion.action?versionId=${deployment.version.id}">
							${deployment.version.name}
						</a>
					</td>

					<td>
						${deployment.version.creationDate}
					</td>

					<td>
						<input data-provide="select-project-checkbox" data-code="${deployment.code}" type="checkbox"/>
					</td>
				</tr>

				[/#list]
			</tbody>

		</table>

		<div class="plugin-mass-deploy-submit-container">

			<div>
				<input type="checkbox" data-provide="safety-check" id="safety-check"/>
				<label for="safety-check">I know what I'm doing</label>
			</div>

			<button class="aui-button aui-button-primary" data-provide="deploy-button" disabled>Promote selected releases to
				`${toEnv}`
			</button>

			<a class="cancel" href="http://devopsreactions.tumblr.com/">No way, I'm scared!</a>

		</div>

	[#else]

		<p>It looks like you have no rights to do anything like mass deployment. Sorry</p>

	[/#if]

</div>
