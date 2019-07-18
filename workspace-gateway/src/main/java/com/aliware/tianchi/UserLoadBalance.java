package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.AtomicPositiveInteger;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcStatus;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    public static final String NAME = "leastactive";

    private final Random random = new Random();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        int length = invokers.size();

        // 1. 获取 活跃数/权重 数组，取最小值，如果有最小值，直接调用对应的，如果相等，随机调用一个
        int minRate = 0;
        int least = -1;
        int[] rates = new int[length];

        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive();
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
            rates[i] = active / weight;
        }

        int flag = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            if (rates[i] < min) {
                min = rates[i];
                flag = i;
            }
        }
        // 如果比例相同，请求权重最高的
        return invokers.get(flag);
    }
}
