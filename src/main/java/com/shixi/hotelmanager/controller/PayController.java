package com.shixi.hotelmanager.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.shixi.hotelmanager.domain.Order;
import com.shixi.hotelmanager.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private OrderMapper orderMapper;
    private static String PRIVATE_KEY="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCl4aRDCJQkS9611Txk0DyNndjgyNOQhhW40je4UUp149WdPz4q1l8u45vzNCbHjLCUWD5QwqEQi5H56ChBGU3/+obt1aStF0WZF5kJAU4RyAX4S4oFQRCRZvg4Jy2ilib3FjrTF+0XX1OC02NafpIccv/WzoWV7zmCo4H3WdJ5cwbod6VukGR3x/shX9JyBaKoO/8NwRp3Yoo44yjNaPvxpimudcSlnAhMrq52ZmzwPM+r5ZSsGeJ+4LmUMUb2qqKdd9wgI/OhDXCKO+rzrdVx4BMva1Tbr4jadirsCQ5Q+meGmwxC4askzsEIfIjgDjhaghSSDQE38biWN7q1sq+tAgMBAAECggEBAIvdNzz2DMKV3hB+3M877PKTNvxBGHFxPPt69FRK5neEROazHl3MJrFIZIOpY1E5xOEvjktV76wdolWOc/J/vY6p0/7Q9mqjhqFQjk5TdVn0x2PVfWh0td2DbqMaFZZS+EO50JuQPu5ICAf06H6y3ctzA1hBBc2nyVvnNXwzlg2jmULnyZp2Doi3zmgjkqRu/u2qARfdJsgePSDUxq14CKxbLDg8Eju3oqfd5tvcRK2S6At2mAuHuNp4VXKCC//WiBToG++iSwCojaPl1XEqiE1WBhkJWiwiuV9FwE1aEgAX2LyEv1oxW5HZzaF0r/txb76f82TH/tSJ0603MHDIooECgYEA8Vgc0bdu8GzfWqMna2Pv8ENkgm3ez7TMx7BKG0X1YYOVU7VpKS1O9xm3N24dPGhFyj+DmbrfZXeEli2xEviJQfhWimzYzuho2vnAZFn3eOM4aAZ97hrR/wea7nIccql9e9uuvvGhXLSPSl+J3T5sg+67R2DhT37/3X/a++CEgHECgYEAr/RmcZm5IIJn6bN18PSPQ8vcp015bhDF3HtNWH8EPTAprv6/CuY6otkIKuN35SWOf1a525PcHb/aAMCXw2NYewbhsou/LIccZYw9xgAgHNQxJTOo1IQwncNnx3u9FA9wky3ixK6eHNo9hSeip7O/p7CTRBqOXZ2p6D+9Eo6bwP0CgYEArug0uqg99nBwzrc/ckzTL0UoKn6F4/IcFvxkOK/SzgEWz7vBot37RImWhs1+0rCfI5w0O8166YZcyJoEosMMdosL7PZFim5Uz54BGLk66JmD36AU0+MMHc/dMMHybAb5sjHbyvZDA3S4BCaJO5Zp/pOdlnVX1M0tkdF/Wtu0K4ECgYAWf63JwNpHKeWXoHboRJ09Egg47FMmm8ZxFuMg+bzVBh+OXMyY3C+LOy0sLsHZ7x91cOV7CkEPHMUHa5j8Ruu9b3fUmMHtM6mR4ojTlJiGlythkmV4Jx8ATUgr3cqjkgXXC/r/I0Tcc5uCNzs5LmbHTnDGOI8TsWFUbTID+XA5EQKBgCAukf992DiMLQbm5liGDQnbiN38TpTfyCHl14omHAE+uX6Vf7TW4YYfghznWu00moTRZ3GYywFP0+q7ss+v2fsVOz0lH/3wosJGxm5YHrRfH2CrHafwQnAKOnp4vgNIkPZ+2N8NCYTIbdoeqSNRpOLaLUectbRAxzc/9IReEkDn";
    private static String ALIPAY_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi4rMalBhqMu6aslBjznhU4D/TfEbyVVEUoPCFclJSC5Ndamb4LMEE88yCr7kjlbRGQhsBhgot8FxprlV6IilHNSQ+vvHQImWGT9L8TE8uHVQljT16y2L7kaPw5DDUoRwdV8Z2NUtz55NoJPYDMCBvVremuk26/pGeexpzKpLAAxubL2tinB/VzrDrawNQiIrJHnRghUsCT+NrlsAZWedcdLRO/PSqR0BGbcpc4sigfreW9w8dPAPqqjQN2Z3pZ/Ho9i7CLgss1W47xnE1kqVFEyL/fMNs4T73xFyeIpSYjLMkEqwnWH+1NIjzyVrI3yBFMW6xMJM/06PqoDWbay8VQIDAQAB";
    @RequestMapping("/create")
    @ResponseBody
    public String createPay(Order order){
        AlipayClient alipayClient=new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do","2016101100658761",PRIVATE_KEY,"json","UTF-8",ALIPAY_PUBLIC_KEY,"RSA2");
        AlipayTradePagePayRequest request=new AlipayTradePagePayRequest();
        request.setReturnUrl("http://localhost:8280/pay/CallBack/return");
        request.setNotifyUrl("http://localhost:8280/pay/CallBack/notify");//在公共参数中设置回跳和通知地址

        request.setBizContent("{" +
                "    \"out_trade_no\":\""+order.getOrderId()+"\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":"+order.getPrice()+"," +
                "    \"subject\":\"iMac pro\"," +
                "    \"body\":\"iMac Pro\"," +
                "    \"timeout_express\":\"1m\","+
                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
                "    \"extend_params\":{" +
                "    \"sys_service_provider_id\":\"2088511833207846\"" +
                "    }"+
                "  }");//填充业务参数
        String form="";
        try {
            AlipayTradePagePayResponse response=alipayClient.pageExecute(request);
            form = response.getBody(); //调用SDK生成表单
            orderMapper.insert(order);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    @ResponseBody
    @RequestMapping("/admin/refund")
    public String refund(Order order) throws AlipayApiException {
        AlipayClient alipayClient=new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do","2016101100658761",PRIVATE_KEY,"json","UTF-8",ALIPAY_PUBLIC_KEY,"RSA2");
        AlipayTradeRefundRequest request=new AlipayTradeRefundRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+order.getOrderId()+"\"," +
                "    \"refund_amount\":"+order.getPrice()+"," +
                "    \"refund_reason\":\"正常退款\"," +
                "    \"out_request_no\":\"HZ01RF001\"," +
                "    \"operator_id\":\"OP001\"," +
                "    \"store_id\":\"NJ_S_001\"," +
                "    \"terminal_id\":\"NJ_T_001\"" +
                "  }");
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if(response.isSuccess()){
            Map<String,Object> map2=new HashMap<>();
            map2.put("order_id",Integer.parseInt(order.getOrderId()));
            Order order2=orderMapper.selectByMap(map2).get(0);
            order2.setStatus("REFUND");
            orderMapper.updateById(order2);
            return "success";
        }else{
            return "fail";
        }

    }


    @RequestMapping("/Callback")
    @ResponseBody
    public String test1(){
        return "test1";
    }

    @RequestMapping("/Callback/test2")
    @ResponseBody
    public String test2(){
        return "test2";
    }

    @RequestMapping("/CallBack/return")
    @ResponseBody
    public String returnPage(HttpServletRequest request) throws AlipayApiException {
        Map<String,String[]> map1 = request.getParameterMap();
        Enumeration<String> names=request.getParameterNames();
        Map<String,String> map=new HashMap<>();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            map.put(name, map1.get(name)[0]);
        }
        System.out.println("收到支付宝通知！！");
        boolean signVerified = AlipaySignature.rsaCheckV1(map, ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");
        if(signVerified){
            System.out.println("通知校验成功！！");
            System.out.println("map:"+map.toString());
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            Map<String,Object> map2=new HashMap<>();
            map2.put("order_id",Integer.parseInt(map.get("out_trade_no")));
            Order order=orderMapper.selectByMap(map2).get(0);
            order.setStatus("PAYED");
            orderMapper.updateById(order);
            return "success";
        }else{
            System.out.println("通知校验失败！！");
            return "failure";
            // TODO 验签失败则记录异常日志，并在response中返回failure.
        }
    }
}
