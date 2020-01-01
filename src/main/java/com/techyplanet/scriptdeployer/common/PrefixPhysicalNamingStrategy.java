package com.techyplanet.scriptdeployer.common;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PrefixPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

	private static final long serialVersionUID = 7810987999913207274L;

	public final String tableNamePrefix;

	public PrefixPhysicalNamingStrategy(String tableNamePrefix) {
		super();
		this.tableNamePrefix = tableNamePrefix;
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		Identifier newIdentifier = new Identifier(tableNamePrefix + name.getText(), name.isQuoted());
		return super.toPhysicalTableName(newIdentifier, context);
	}
}
