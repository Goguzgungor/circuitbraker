package com.example.circuitbraker.service;


import com.example.circuitbraker.models.CircuitBreakerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CircuitBreakerService {
    private Map<String, List<CircuitBreakerModel>> map;
    public CircuitBreakerService() {
        this.map = new HashMap<>();
    }

    public void addMethodCount(Object payload, String methodName, int httpStatus, String errorMessage) {
        if (map.containsKey(methodName)) {
            Date currentDate = new Date();
            List<CircuitBreakerModel> recentCallsList = map.get(methodName);
            CircuitBreakerModel lastInvoked = recentCallsList.get(recentCallsList.size() - 1);

            CircuitBreakerModel circuitBreaker =  new CircuitBreakerModel(methodName, lastInvoked.getCount()+1,currentDate, httpStatus, payload.toString(),errorMessage);
            int callLimit = 5;
            long timeDifference = (currentDate.getTime() - lastInvoked.getLastInvoked().getTime());
            long difference_In_Seconds
                    = (timeDifference
                    / 1000)
                    % 60;
            log.info("Time Difference: "+difference_In_Seconds);
            lastInvoked.setLastInvoked(currentDate);
            recentCallsList.set(recentCallsList.size() - 1, lastInvoked);
            map.put(methodName, recentCallsList);
            if(difference_In_Seconds<10){
                recentCallsList.add(circuitBreaker);
                map.put(methodName, recentCallsList);
            }

        } else {
            List<CircuitBreakerModel> recentCallsList = new ArrayList<>();
            CircuitBreakerModel circuitBreaker =  new CircuitBreakerModel(methodName,1,new Date(), httpStatus, payload.toString(),errorMessage);
            recentCallsList.add(circuitBreaker);
            map.put(methodName, recentCallsList);
        }
    }
    public int getMethodCount(String methodName) {
        if (map.containsKey(methodName)) {
            List<CircuitBreakerModel> recentCallsList = map.get(methodName);
            return recentCallsList.size();
        } else {
            return 0;
        }
    }
    public long LastErrorsTimeAfter5th(String methodName) {
        if (map.containsKey(methodName)) {
            if (map.get(methodName).size() >= 5) {
                List<CircuitBreakerModel> recentCallsList = map.get(methodName);
                CircuitBreakerModel CircuitBreakerModel = recentCallsList.get(4);
                return CircuitBreakerModel.getLastInvoked().getTime();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    private void refreshList(String methodName){
        if (map.containsKey(methodName)) {
            List<CircuitBreakerModel> recentCallsList = map.get(methodName);
            map.remove(methodName);
        }
    }
    public boolean isCircuitOpen (String methodName){
        Date currentTime = new Date();
        long timeAfter5thError = LastErrorsTimeAfter5th(methodName);
        long timeDifference = Math.abs(currentTime.getTime() - timeAfter5thError);
        long secondsDifference = timeDifference / 1000;
        int timeLimit = 10;
        if( secondsDifference<= timeLimit){
            return true;
        }else{
            this.refreshList(methodName);
            return false;
        }
    }
}
