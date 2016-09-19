/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.epam.ta.reportportal.core.launch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.google.common.base.CharMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.epam.ta.reportportal.core.launch.impl.UpdateLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;

/**
 * @author Dzmitry_Kavalets
 */
public class UpdateLaunchHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void moveLaunchToDebugByCustomer() {
		final String toDebug = "toDebug";
		final String projectId = "project";
		final String user = "customer";
		final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
		updateLaunchRQ.setMode(Mode.DEBUG);

		final Launch launch = new Launch();
		launch.setId(toDebug);
		final LaunchRepository launchRepository = mock(LaunchRepository.class);
		when(launchRepository.findOne(toDebug)).thenReturn(launch);

		final ProjectRepository projectRepository = mock(ProjectRepository.class);
		final Project project = buildProject(projectId, user);
		when(projectRepository.findOne(projectId)).thenReturn(project);

		final UpdateLaunchHandler updateLaunchHandler = new UpdateLaunchHandler();
		updateLaunchHandler.setLaunchRepository(launchRepository);
		updateLaunchHandler.setProjectRepository(projectRepository);
		updateLaunchHandler.setUserRepository(mock(UserRepository.class));

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.clearPlaceholders(ErrorType.ACCESS_DENIED.getDescription()).trim());
		updateLaunchHandler.updateLaunch(toDebug, projectId, user, updateLaunchRQ);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Project buildProject(String projectId, String user) {
		final Project project = new Project();
		project.setName(projectId);
		final HashMap<String, Project.UserConfig> users = new HashMap();
		final Project.UserConfig userConfig = new Project.UserConfig();
		userConfig.setProjectRole(ProjectRole.CUSTOMER);
		users.put(user, userConfig);
		project.setUsers(users);
		return project;
	}
}