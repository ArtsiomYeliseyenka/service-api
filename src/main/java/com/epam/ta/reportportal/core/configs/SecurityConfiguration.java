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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.UserRoleHierarchy;
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.List;

/**
 * Spring's Security Configuration
 *
 * @author Andrei Varabyeu
 */
@Configuration
class SecurityConfiguration {

    @Bean
    public PermissionEvaluatorFactoryBean permissionEvaluator() {
        return new PermissionEvaluatorFactoryBean();
    }

    @Configuration
    @EnableGlobalMethodSecurity(proxyTargetClass = true, prePostEnabled = true)
    public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

        @Autowired
        private RoleHierarchy roleHierarchy;

        @Autowired
        private PermissionEvaluator permissionEvaluator;

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            handler.setRoleHierarchy(roleHierarchy);
            handler.setPermissionEvaluator(permissionEvaluator);
            return handler;
        }

    }

    @Configuration
    @EnableResourceServer
    public static class SecurityServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private PermissionEvaluator permissionEvaluator;

        @Bean
        public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
            return new PermissionEvaluatorFactoryBean();
        }

        @Bean
        public static RoleHierarchy userRoleHierarchy() {
            return new UserRoleHierarchy();
        }

        private DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
            OAuth2WebSecurityExpressionHandler handler = new OAuth2WebSecurityExpressionHandler();
            handler.setRoleHierarchy(userRoleHierarchy());
            handler.setPermissionEvaluator(permissionEvaluator);
            return handler;
        }

        private AccessDecisionManager webAccessDecisionManager() {
            List<AccessDecisionVoter<?>> accessDecisionVoters = Lists.newArrayList();
            accessDecisionVoters.add(new AuthenticatedVoter());
            WebExpressionVoter webVoter = new WebExpressionVoter();
            webVoter.setExpressionHandler(webSecurityExpressionHandler());
            accessDecisionVoters.add(webVoter);

            return new AffirmativeBased(accessDecisionVoters);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .accessDecisionManager(webAccessDecisionManager())
                    .antMatchers(
                            "/**/user/registration/info*",
                            "/**/user/registration**",
                            "/**/user/password/reset/*",
                            "/**/user/password/reset",
                            "/**/user/password/restore**",

                            "/documentation.html")
                        .permitAll()
                    .antMatchers(
                            "/v2/**",
                            "/swagger-resources",
                            "/certificate/**",
                            "/api/**",
                            "/**")
                        .hasRole("USER")
                    .anyRequest().authenticated()
                    .and().csrf().disable();
        }

    }

}


