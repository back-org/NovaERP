package com.novaerp.service;

import com.novaerp.web.dto.responses.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Integer year);
}
