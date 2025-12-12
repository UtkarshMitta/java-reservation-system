package edu.nyu.cs9053.reservo.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    @Autowired
    private ReservationService reservationService;

    @Scheduled(fixedDelayString = "${reservo.scheduler.interval-seconds:5}000")
    public void processExpiredHolds() {
        reservationService.expireHolds();
    }
}

