package cn.jia.dwz.service.impl;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DataUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.dwz.dao.DwzRecordMapper;
import cn.jia.dwz.entity.DwzRecord;
import cn.jia.dwz.service.DwzService;
import cn.jia.user.common.ErrorConstants;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DwzServiceImpl implements DwzService {

    @Autowired
    private DwzRecordMapper dwzRecordMapper;

    @Override
    public DwzRecord view(String uri) throws Exception {
        DwzRecord record = dwzRecordMapper.selectByUri(uri);
        if(record == null) {
            throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
        }
        long now = DateUtil.genTime(new Date());
        DwzRecord upRecord = new DwzRecord();
        upRecord.setId(record.getId());
        upRecord.setUpdateTime(now);
        upRecord.setPv(record.getPv() + 1);
        dwzRecordMapper.updateByPrimaryKeySelective(upRecord);
        return record;
    }

    @Override
    public String gen(String jiacn, String orgi, Long expireTime) {
        DwzRecord dwzRecord = new DwzRecord();
        dwzRecord.setJiacn(jiacn);
        dwzRecord.setOrgi(orgi);
        Page<DwzRecord> list = dwzRecordMapper.selectByExample(dwzRecord);
        if (list.size() > 0) {
            return list.get(0).getUri();
        } else {
            dwzRecord.setUri(DataUtil.getRandom(false, 8));
            Long now = DateUtil.genTime(new Date());
            dwzRecord.setCreateTime(now);
            dwzRecord.setUpdateTime(now);
            if (expireTime == null) {
                expireTime = now + 360 * 24 * 60 * 60;
            }
            dwzRecord.setExpireTime(expireTime);
            dwzRecordMapper.insertSelective(dwzRecord);
            return dwzRecord.getUri();
        }
    }

    @Override
    public DwzRecord find(Integer id) throws Exception {
        DwzRecord record = dwzRecordMapper.selectByPrimaryKey(id);
        if(record == null) {
            throw new EsRuntimeException(cn.jia.point.common.ErrorConstants.GIFT_NOT_EXISTS);
        }
        return record;
    }

    @Override
    public DwzRecord update(DwzRecord record) {
        Long now = DateUtil.genTime(new Date());
        record.setUpdateTime(now);
        dwzRecordMapper.updateByPrimaryKeySelective(record);
        return record;
    }

    @Override
    public void delete(Integer id) {
        dwzRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Page<DwzRecord> list(DwzRecord plan, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return dwzRecordMapper.selectByExample(plan);
    }
}
