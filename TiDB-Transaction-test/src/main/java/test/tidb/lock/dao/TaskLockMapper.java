package test.tidb.lock.dao;

import org.apache.ibatis.annotations.Param;
import test.tidb.lock.entity.TaskLock;

public interface TaskLockMapper {
    int deleteByPrimaryKey(String taskName);

    int insert(TaskLock record);

    int insertSelective(TaskLock record);

    TaskLock selectByPrimaryKey(String taskName);

    int updateByPrimaryKeySelective(TaskLock record);

    int updateByPrimaryKey(TaskLock record);


    //
    int insertIgnore(TaskLock record);

    TaskLock selectForUpdate(String taskName);

    int updateLockFlagNoCheck(TaskLock record);

    int updateUnlockFlagNoCheck(TaskLock record);

    int updateLockFlagWithCheck(@Param("newRecord") TaskLock newRecord, @Param("oldRecord") TaskLock oldRecord);

    int selectCount(String taskName);
}