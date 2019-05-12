package com.techyplanet.scriptdeployer.repository;

import org.springframework.data.repository.CrudRepository;

import com.techyplanet.scriptdeployer.entity.ScriptHistory;

public interface ScriptHistoryRepository extends CrudRepository<ScriptHistory, Long> {

	ScriptHistory findBySequenceAndPattern(Long sequence, String pattern);

	ScriptHistory findByPath(String path);
}