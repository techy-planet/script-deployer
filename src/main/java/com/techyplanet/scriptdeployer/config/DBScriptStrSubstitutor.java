package com.techyplanet.scriptdeployer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.techyplanet.scriptdeployer.component.AppSettings;

@Configuration
public class DBScriptStrSubstitutor {

	@Autowired
	private Environment env;

	@Autowired
	private AppSettings appSettings;

	@Bean
	public StringSubstitutor dbVariablesSubstitutor() {
		String[] variableNames = appSettings.getScriptDBVariables().split(",");
		Map<String, String> dbScriptVariables = new HashMap<>(variableNames.length);
		for (String variableName : variableNames) {
			dbScriptVariables.put(variableName.trim(), env.getProperty(variableName));
		}
		return new StringSubstitutor(dbScriptVariables);
	}

}
