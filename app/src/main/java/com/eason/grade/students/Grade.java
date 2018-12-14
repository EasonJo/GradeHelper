package com.eason.grade.students;

import com.eason.grade.bean.gen.DaoSession;
import com.eason.grade.bean.gen.GradeDao;
import com.eason.grade.bean.gen.StudentDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.*;

/**
 * Grade bean
 *
 * @author Eason
 */
@Entity
public class Grade {
    /**
     * grade ID
     */
    @Id(autoincrement = true)
    private Long id;

    @Unique
    private Long sid;

    /**
     * {@link Student}
     */
    @ToOne(joinProperty = "sid")
    private Student student;

    private String gradeName;

    /**
     * right or wrong
     */
    private int isRight = -1;

    /**
     * is read or not
     */
    private boolean isRead;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 681281562)
    private transient GradeDao myDao;

    @Generated(hash = 2042976393)
    public Grade() {
    }

    @Generated(hash = 666227939)
    public Grade(Long id, Long sid, String gradeName, int isRight, boolean isRead) {
        this.id = id;
        this.sid = sid;
        this.gradeName = gradeName;
        this.isRight = isRight;
        this.isRead = isRead;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getGradeName() {
        return this.gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public int getIsRight() {
        return this.isRight;
    }

    public void setIsRight(int isRight) {
        this.isRight = isRight;
    }

    @Generated(hash = 79695740)
    private transient Long student__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 906694816)
    public Student getStudent() {
        Long __key = this.sid;
        if (student__resolvedKey == null || !student__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            StudentDao targetDao = daoSession.getStudentDao();
            Student studentNew = targetDao.load(__key);
            synchronized (this) {
                student = studentNew;
                student__resolvedKey = __key;
            }
        }
        return student;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1530825122)
    public void setStudent(Student student) {
        synchronized (this) {
            this.student = student;
            sid = student == null ? null : student.getSid();
            student__resolvedKey = sid;
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
    @Generated(hash = 1187286414)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGradeDao() : null;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", sid=" + sid +
                ", student=" + student +
                ", gradeName='" + gradeName + '\'' +
                ", isRight=" + isRight +
                ", isRead=" + isRead +
                '}';
    }
}
