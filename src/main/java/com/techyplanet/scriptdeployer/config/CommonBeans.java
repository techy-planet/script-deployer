package com.techyplanet.scriptdeployer.config;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.techyplanet.scriptdeployer.common.PrefixPhysicalNamingStrategy;
import com.techyplanet.scriptdeployer.component.AppSettings;

@Configuration
public class CommonBeans {

	@Autowired
	private AppSettings appSettings;

	@Bean
	public PhysicalNamingStrategy physical() {
		return new PrefixPhysicalNamingStrategy(appSettings.getTableNamePrefix());
	}

	@Bean
	public ImplicitNamingStrategy implicit() {
		return new ImplicitNamingStrategyLegacyJpaImpl();
	}
}
