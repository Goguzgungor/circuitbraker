//package com.example.circuitbraker;
//
//
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class CircuitBrakerService {
//    private Map<String, Map<String,String>> map;
//
//
//    void addMethodCount(Object payload, String methodName) {
//        if (map.containsKey(methodName)) {
//            Map<String,String> methodMap = map.get(methodName);
//            if (methodMap.containsKey(payload)) {
//                methodMap.put(payload, methodMap.get(payload) + 1);
//            } else {
//                methodMap.put(payload, 1);
//            }
//        } else {
//            Map<String,String> methodMap = new HashMap<>();
//            methodMap.put(payload, 1);
//            map.put(methodName, methodMap);
//        }
//    }
//}
