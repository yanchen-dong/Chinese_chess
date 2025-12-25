package com.ydc.chess.controller;

import com.ydc.chess.network.NetworkService;

/**
 * 网络管理器
 * 用于在不同控制器之间共享网络服务实例
 */
public class NetworkManager {
    private static NetworkService networkService;
    private static boolean isNetworkMode = false;
    
    public static void setNetworkService(NetworkService service) {
        networkService = service;
        isNetworkMode = (service != null);
    }
    
    public static NetworkService getNetworkService() {
        return networkService;
    }
    
    public static boolean isNetworkMode() {
        return isNetworkMode;
    }
    
    public static void clear() {
        networkService = null;
        isNetworkMode = false;
    }
}

