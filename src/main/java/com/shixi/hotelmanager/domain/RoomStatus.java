package com.shixi.hotelmanager.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tbl_room_status")
public class RoomStatus extends Model<RoomStatus> {

    private int id;
    private int roomId;
    private Date recordForDate;
    private int roomNum;//房间编号
}
