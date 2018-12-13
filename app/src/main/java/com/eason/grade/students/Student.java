package com.eason.grade.students;

import com.eason.grade.bean.gen.ClassesDao;
import com.eason.grade.bean.gen.DaoSession;
import com.eason.grade.bean.gen.StudentDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Student bean
 *
 * @author Eason
 */
@Entity
public class Student {

    /**
     * stu number
     */
    @Id(autoincrement = true)
    private Long sid;

    private String studentNo;

    /**
     * name
     */
    private String name;

    /**
     * age
     */
    private int age;

    private Long cid;

    /**
     * class Name
     */
    @ToOne(joinProperty = "cid")
    private Classes classes;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1943931642)
    private transient StudentDao myDao;

    @Generated(hash = 142132516)
    public Student(Long sid, String studentNo, String name, int age, Long cid) {
        this.sid = sid;
        this.studentNo = studentNo;
        this.name = name;
        this.age = age;
        this.cid = cid;
    }

    @Generated(hash = 1556870573)
    public Student() {
    }

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getCid() {
        return this.cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    @Generated(hash = 929604645)
    private transient Long classes__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 223583781)
    public Classes getClasses() {
        Long __key = this.cid;
        if (classes__resolvedKey == null || !classes__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ClassesDao targetDao = daoSession.getClassesDao();
            Classes classesNew = targetDao.load(__key);
            synchronized (this) {
                classes = classesNew;
                classes__resolvedKey = __key;
            }
        }
        return classes;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 168680912)
    public void setClasses(Classes classes) {
        synchronized (this) {
            this.classes = classes;
            cid = classes == null ? null : classes.getId();
            classes__resolvedKey = cid;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1701634981)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getStudentDao() : null;
    }

    @Override
    public String toString() {
        return "Student{" +
                "sid=" + sid +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", cid=" + cid +
                // ", classes=" + classes +
                '}';
    }

    public String getStudentNo() {
        return this.studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}
