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

package com.epam.ta.reportportal.core.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.epam.ta.reportportal.config.MongodbConfiguration;
import com.github.mongobee.Mongobee;
import com.mongodb.MongoClient;

/**
 * Mongo Migration tool configs
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class MigrationConfiguration {
	@Autowired
	private MongodbConfiguration.MongoProperies mongoProperties;
	@Autowired
	private Environment environment;

	@Bean
	@Autowired
	@Profile({ "!unittest" })
	public Mongobee mongobee(MongoClient mongoClient) {
		Mongobee runner = new Mongobee(mongoClient);
		runner.setDbName(mongoProperties.getDbName());
		runner.setChangeLogsScanPackage("com.epam.ta.reportportal.migration");
		runner.setSpringEnvironment(environment);
		return runner;
	}
}
