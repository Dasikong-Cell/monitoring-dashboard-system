package com.monitor.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.dashboard.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {}
