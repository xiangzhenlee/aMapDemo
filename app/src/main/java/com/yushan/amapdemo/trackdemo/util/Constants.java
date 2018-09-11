package com.yushan.amapdemo.trackdemo.util;

public class Constants {

    /**
     * 终端名称，该名称可以根据使用方业务而定，比如可以是用户名、用户手机号等唯一标识
     *
     * 通常应该使用该名称查询对应终端id，确定该终端是否存在，如果不存在则创建，然后就可以开启轨迹上报，将上报的轨迹关联
     * 到该终端
     */
    public static final String TERMINAL_NAME = "test_terminal_name";

    /**
     * 服务id，请修改成您创建的服务的id
     *
     * 猎鹰轨迹服务与猎鹰SDK的强关联暂未开放，您在使用相关产品之前应该分别申请Web，android sdk和ios sdk的api key
     * (根据所使用的服务具体选择），然后使用web的api key创建Service，创建完成后请将Service Id同android和iOS的
     * Key通过工单提交给我们，由我们为您进行关联绑定，然后您就可以正常接入猎鹰sdk来使用相关服务。
     */
    public static final long SERVICE_ID = 2260;
}
