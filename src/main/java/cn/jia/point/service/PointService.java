package cn.jia.point.service;

import cn.jia.point.entity.Record;
import cn.jia.point.entity.Referral;
import cn.jia.point.entity.Sign;

public interface PointService {
	
	Record init(Record record) throws Exception;
	
	Record sign(Sign sign) throws Exception;
	
	Record referral(Referral referral) throws Exception;
	
	Record luck(Record record) throws Exception;

	Record add(String jiacn, int point, int type) throws Exception;
}
