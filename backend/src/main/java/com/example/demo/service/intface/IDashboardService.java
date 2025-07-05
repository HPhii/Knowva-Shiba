package com.example.demo.service.intface;

import com.example.demo.model.io.dto.dashboard.*;

public interface IDashboardService {
    OverviewStats getOverviewStats();
    UserStats getUserStats();
    ContentStats getContentStats();
}
