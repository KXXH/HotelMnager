package com.shixi.hotelmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixi.hotelmanager.domain.DTO.OrderDTO.CreateOrderDTO;
import com.shixi.hotelmanager.domain.*;
import com.shixi.hotelmanager.mapper.HotelRoomMapper;
import com.shixi.hotelmanager.mapper.HotelStatusMapper;
import com.shixi.hotelmanager.mapper.OrderMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private HotelStatusMapper hotelStatusMapper;

    @Resource
    private HotelRoomMapper hotelRoomMapper;

    @Override
    @Transactional
    public boolean createOrder(CreateOrderDTO dto) {
        Order order=new Order();
        //填写订单基本信息
        order.setOrderRoomId(dto.getOrderRoomId());
        order.setRoomCount(dto.getRoomCount());
        order.setDateStart(dto.getDateStart());
        order.setDateEnd(dto.getDateEnd());
        order.setTelephone(dto.getTelephone());
        order.setPersonName(dto.getPersonName());
        order.setPeopleCount(dto.getPeopleCount());
        //找到操作用户并将用户和订单关联
        User opUser= ((UserDetail)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        order.setOrderUserId(opUser.getUserId());

        //找到对应酒店房间并关联
        HotelRoom room=new HotelRoom();
        room=room.selectById(order.getOrderRoomId());
        order.setPrice(room.getPrice());
        order.setBreakfast(room.getBreakfast());
        order.setWindows(room.getWindows());

        Hotel hotel=new Hotel();
        hotel=hotel.selectById(room.getTheRoomHotelId());
        order.setHotelName(hotel.getHotelName());

        //设置订单当前状态为未支付
        order.setStatus("UNPAID");

        //保存订单
        save(order);

        //TODO:为房间状态和酒店状态数据表加锁

        HotelStatus hotelStatus=new HotelStatus();
        RoomStatus roomStatus=new RoomStatus();

        int select_count=0;
        //写房间状态数据库
        QueryWrapper<RoomStatus> roomQueryWrapper=new QueryWrapper<>();
        roomQueryWrapper
                .eq("room_id",order.getOrderRoomId())
                .ge("record_for_date",order.getDateStart())
                .le("record_for_date",order.getDateEnd());
        //找到合适的房间
        for(int i=0;i<order.getRoomCount();i++){
            
        }
        return false;
    }

    @Override
    public boolean payOrder(Long orderId) {
        return false;
    }

    @Override
    @Transactional
    public boolean refundOrder(Long orderId) {

        System.out.println("==============================");
        //根据订单号获取订单
        Order order = new Order();
        QueryWrapper<Order> query = new QueryWrapper<>();
        query.eq("order_id",orderId);
        order = order.selectOne(query);
        //得到开始时间和结束时间
        String dateStart = order.getDateStart();
        String dateEnd = order.getDateEnd();

        Date start = null,end=null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            start = sdf.parse(dateStart);
            end = sdf.parse(dateEnd);
            System.out.println(start+"->"+end);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (start.getTime()==end.getTime())
            updateDB(order,dateStart);
        else{
            for(Date date = start; date.getTime() != end.getTime(); date = dateAdd(date)){
                String useDate = sdf.format(date);
                System.out.println("date:"+useDate);

                updateDB(order,useDate);

            }
        }

        return true;
    }

    public Date dateAdd(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,1); //把日期往后增加一天,整数  往后推,负数往前移动
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果
        return date;
    }

    public void updateDB(Order order,String useDate){
        //得到酒店ID
        int hotelId = order.getHotelId();
        //根据酒店Id得到实例
        HotelStatus hotelStatus = new HotelStatus();
        QueryWrapper<HotelStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hotel_id",hotelId).eq("record_for_date",useDate);
        hotelStatus = hotelStatus.selectOne(queryWrapper);
        hotelStatus.setHotelRoomOrdered(hotelStatus.getHotelRoomOrdered()-order.getRoomCount());
        hotelStatus.updateById();

        //得到房间ID
        int theRoomId = order.getOrderRoomId();
        RoomStatus roomStatus = new RoomStatus();
        String[] nums = order.getRoomNums().split(",");
        for(int i = 0; i < nums.length; i++){
            QueryWrapper<RoomStatus> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("room_id",theRoomId).eq("record_for_date",useDate).eq("room_num",nums[i]);
            roomStatus.delete(queryWrapper1);
        }
    }
}
